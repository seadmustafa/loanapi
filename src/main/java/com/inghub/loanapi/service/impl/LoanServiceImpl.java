package com.inghub.loanapi.service.impl;


import com.inghub.loanapi.dto.*;
import com.inghub.loanapi.dto.mapper.LoanMapper;
import com.inghub.loanapi.entity.Customer;
import com.inghub.loanapi.entity.Loan;
import com.inghub.loanapi.entity.LoanInstallment;
import com.inghub.loanapi.enums.InstallmentStatus;
import com.inghub.loanapi.enums.LoanStatus;
import com.inghub.loanapi.exception.CustomerNotFoundException;
import com.inghub.loanapi.exception.InsufficientCreditException;
import com.inghub.loanapi.exception.InvalidLoanParameterException;
import com.inghub.loanapi.exception.LoanAlreadyPaidException;
import com.inghub.loanapi.repository.CustomerRepository;
import com.inghub.loanapi.repository.LoanRepository;
import com.inghub.loanapi.service.LoanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class LoanServiceImpl implements LoanService {

    private final CustomerRepository customerRepository;
    private final LoanRepository loanRepository;
    private final LoanMapper loanMapper;

    private static final List<Integer> ALLOWED_INSTALLMENTS = Arrays.asList(6, 9, 12, 24);
    private static final BigDecimal MIN_INTEREST_RATE = new BigDecimal("0.1");
    private static final BigDecimal MAX_INTEREST_RATE = new BigDecimal("0.5");

    @Override
    public LoanResponse createLoan(LoanRequest request) {
        log.info("Starting loan creation for customerId: {}", request.getCustomerId());
        validateLoanParameters(request);

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> {
                    log.error("Customer not found with ID: {}", request.getCustomerId());
                    return new CustomerNotFoundException("Customer not found with ID: " + request.getCustomerId());
                });

        BigDecimal totalLoanAmount = calculateTotalAmount(request.getAmount(), request.getInterestRate());

        if (customer.getUsedCreditLimit().add(totalLoanAmount).compareTo(customer.getCreditLimit()) > 0) {
            log.error("Customer with ID: {} has insufficient credit limit", request.getCustomerId());
            throw new InsufficientCreditException("Insufficient credit limit for this loan.");
        }

        Loan loan = initializeLoan(request, customer, totalLoanAmount);
        List<LoanInstallment> installments = createInstallments(totalLoanAmount, request.getNumberOfInstallments(), loan);
        loan.setInstallments(installments);

        customer.setUsedCreditLimit(customer.getUsedCreditLimit().add(totalLoanAmount));
        customerRepository.save(customer);

        Loan savedLoan = loanRepository.save(loan);
        log.info("Loan created successfully for customerId: {} with loanId: {}", customer.getId(), savedLoan.getId());

        return loanMapper.toLoanResponse(savedLoan);
    }

    private BigDecimal calculateTotalAmount(BigDecimal amount, BigDecimal interestRate) {
        return amount.multiply(BigDecimal.ONE.add(interestRate)).setScale(2, RoundingMode.HALF_UP);
    }

    private Loan initializeLoan(LoanRequest request, Customer customer, BigDecimal totalLoanAmount) {
        Loan loan = new Loan();
        loan.setCustomer(customer);
        loan.setLoanAmount(request.getAmount().setScale(2, RoundingMode.HALF_UP));
        loan.setInterestRate(request.getInterestRate());
        loan.setNumberOfInstallments(request.getNumberOfInstallments());
        loan.setTotalAmount(totalLoanAmount);
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setIsPaid(false);
        return loan;
    }

    private List<LoanInstallment> createInstallments(BigDecimal totalAmount, int numInstallments, Loan loan) {
        List<LoanInstallment> installments = new ArrayList<>();
        BigDecimal installmentAmount = totalAmount.divide(BigDecimal.valueOf(numInstallments), 2, RoundingMode.HALF_UP);
        LocalDate firstDueDate = LocalDate.now().plusMonths(1).with(TemporalAdjusters.firstDayOfMonth());

        for (int i = 0; i < numInstallments; i++) {
            LoanInstallment installment = new LoanInstallment();
            installment.setLoan(loan);
            installment.setInstallmentNumber(i + 1);
            installment.setDueDate(firstDueDate.plusMonths(i));
            installment.setAmount(installmentAmount);
            installment.setIsPaid(false);
            installment.setStatus(InstallmentStatus.PENDING);
            installments.add(installment);
        }

        return installments;
    }

    @Override
    public List<LoanResponse> listLoans(Long customerId, Integer numInstallments, Boolean isPaid) {
        log.info("Fetching loans for customerId: {}", customerId);
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> {
                    log.error("Customer not found with ID: {}", customerId);
                    return new CustomerNotFoundException("Customer not found");
                });

        List<Loan> loans = loanRepository.findByCustomerId(customerId);
        loans = filterLoans(loans, numInstallments, isPaid);
        log.info("Found {} loans for customerId: {}", loans.size(), customerId);

        return loanMapper.toLoanResponseList(loans);
    }

    private List<Loan> filterLoans(List<Loan> loans, Integer numInstallments, Boolean isPaid) {
        return loans.stream()
                .filter(loan -> numInstallments == null || loan.getNumberOfInstallments().equals(numInstallments))
                .filter(loan -> isPaid == null || loan.getIsPaid().equals(isPaid))
                .collect(Collectors.toList());
    }

    @Override
    public List<InstallmentResponse> listInstallmentsForLoan(Long loanId) {
        log.info("Listing installments for loanId: {}", loanId);
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> {
                    log.error("Loan not found with ID: {}", loanId);
                    return new IllegalArgumentException("Loan not found");
                });

        return loanMapper.toInstallmentResponseList(loan.getInstallments());
    }


    @Override
    public LoanPaymentResponse payLoanInstallment(LoanPaymentRequest paymentRequest) {
        final long loanId = paymentRequest.getLoanId();
        log.info("Start processing payment for loanId: {}", loanId);

        validatePaymentAmount(paymentRequest.getPaymentAmount());

        Loan loan = fetchLoan(loanId);
        validateLoanNotPaid(loan);

        Customer customer = fetchCustomer(loan.getCustomer().getId());

        List<LoanInstallment> unpaidInstallments = getUnpaidInstallments(loan);
        BigDecimal remainingPayment = paymentRequest.getPaymentAmount();
        BigDecimal totalPaidAmount = BigDecimal.ZERO;

        List<LoanInstallment> paidInstallments = processInstallments(unpaidInstallments, remainingPayment);
        totalPaidAmount = calculateTotalPaid(paidInstallments);

        boolean loanFullyPaid = unpaidInstallments.stream().allMatch(LoanInstallment::getIsPaid);
        loan.setIsPaid(loanFullyPaid);
        loanRepository.save(loan);

        updateCustomerCreditLimit(customer, totalPaidAmount);
        log.info("Payment processed for loanId: {}. Total paid: {}, Number of installments paid: {}, Loan fully paid: {}",
                loanId, totalPaidAmount, paidInstallments.size(), loanFullyPaid);

        return buildLoanPaymentResponse(loanId, totalPaidAmount, paidInstallments, loanFullyPaid);
    }

    private void validatePaymentAmount(BigDecimal paymentAmount) {
        if (paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            log.error("Payment amount must be positive. Amount: {}", paymentAmount);
            throw new IllegalArgumentException("Payment amount must be greater than zero.");
        }
    }

    private Loan fetchLoan(long loanId) {
        return loanRepository.findById(loanId)
                .orElseThrow(() -> {
                    log.error("Loan not found with ID: {}", loanId);
                    return new IllegalArgumentException("Loan not found.");
                });
    }

    private void validateLoanNotPaid(Loan loan) {
        if (loan.getIsPaid()) {
            log.warn("Loan with ID: {} is already fully paid", loan.getId());
            throw new LoanAlreadyPaidException("Loan is already fully paid.");
        }
    }

    private Customer fetchCustomer(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> {
                    log.error("Customer not found with ID: {}", customerId);
                    return new IllegalStateException("Customer associated with loan not found.");
                });
    }

    private List<LoanInstallment> getUnpaidInstallments(Loan loan) {
        return loan.getInstallments().stream()
                .filter(installment -> !installment.getIsPaid())
                .sorted(Comparator.comparing(LoanInstallment::getDueDate))
                .toList();
    }

    private List<LoanInstallment> processInstallments(List<LoanInstallment> installments, BigDecimal payment) {
        LocalDate currentDate = LocalDate.now(ZoneId.of("Europe/Istanbul"));
        LocalDate latestPayableDate = currentDate.plusMonths(3);
        List<LoanInstallment> paidInstallments = new ArrayList<>();

        for (LoanInstallment installment : installments) {
            if (installment.getDueDate().isAfter(latestPayableDate)) {
                log.warn("Skipping installment {} due to future due date {}", installment.getId(), installment.getDueDate());
                continue;
            }

            if (payment.compareTo(installment.getAmount()) >= 0) {
                BigDecimal paidAmount = calculatePaidAmount(installment, currentDate);
                installment.setPaidAmount(paidAmount);
                installment.setIsPaid(true);
                installment.setStatus(InstallmentStatus.PAID);

                payment = payment.subtract(installment.getAmount());
                paidInstallments.add(installment);

                log.info("Installment {} paid. Paid Amount: {}, Remaining payment: {}",
                        installment.getId(), paidAmount, payment);
            } else {
                log.info("Insufficient payment for installment {}. Skipping.", installment.getId());
                break;
            }

            if (payment.compareTo(BigDecimal.ZERO) == 0) break;
        }

        return paidInstallments;
    }

    private BigDecimal calculatePaidAmount(LoanInstallment installment, LocalDate currentDate) {
        long daysDifference = ChronoUnit.DAYS.between(currentDate, installment.getDueDate());
        BigDecimal paidAmount = installment.getAmount();

        if (daysDifference > 0) {
            BigDecimal discount = installment.getAmount().multiply(BigDecimal.valueOf(0.001)).multiply(BigDecimal.valueOf(daysDifference));
            paidAmount = paidAmount.subtract(discount).max(BigDecimal.ZERO);
            log.debug("Early payment discount for installment {}: {}", installment.getId(), discount);
        } else if (daysDifference < 0) {
            BigDecimal penalty = installment.getAmount().multiply(BigDecimal.valueOf(0.001)).multiply(BigDecimal.valueOf(Math.abs(daysDifference)));
            paidAmount = paidAmount.add(penalty);
            log.debug("Late payment penalty for installment {}: {}", installment.getId(), penalty);
        }

        return paidAmount;
    }

    private BigDecimal calculateTotalPaid(List<LoanInstallment> paidInstallments) {
        return paidInstallments.stream()
                .map(LoanInstallment::getPaidAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void updateCustomerCreditLimit(Customer customer, BigDecimal totalPaidAmount) {
        if (totalPaidAmount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal newUsedCreditLimit = customer.getUsedCreditLimit().subtract(totalPaidAmount).max(BigDecimal.ZERO);
            customerRepository.updateUsedCreditLimit(customer.getId(), newUsedCreditLimit);
            log.info("Customer {} credit limit updated. New used credit: {}", customer.getId(), newUsedCreditLimit);
        }
    }

    private LoanPaymentResponse buildLoanPaymentResponse(long loanId, BigDecimal totalPaidAmount,
                                                         List<LoanInstallment> paidInstallments, boolean loanFullyPaid) {
        return LoanPaymentResponse.builder()
                .loanId(loanId)
                .totalPaid(totalPaidAmount)
                .numberOfInstallmentsPaid(paidInstallments.size())
                .loanFullyPaid(loanFullyPaid)
                .paidInstallments(loanMapper.toInstallmentResponseList(paidInstallments))
                .build();
    }


    private void validateLoanParameters(LoanRequest request) {
        if (!ALLOWED_INSTALLMENTS.contains(request.getNumberOfInstallments())) {
            throw new InvalidLoanParameterException("Invalid number of installments.");
        }
        if (request.getInterestRate().compareTo(MIN_INTEREST_RATE) < 0 ||
                request.getInterestRate().compareTo(MAX_INTEREST_RATE) > 0) {
            throw new InvalidLoanParameterException("Interest rate must be between 0.1 and 0.5");
        }
    }
}
