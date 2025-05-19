package com.inghub.loanapi.utils;


import com.inghub.loanapi.entity.Customer;
import com.inghub.loanapi.entity.Loan;
import com.inghub.loanapi.entity.LoanInstallment;
import com.inghub.loanapi.entity.User;
import com.inghub.loanapi.enums.InstallmentStatus;
import com.inghub.loanapi.enums.LoanStatus;
import com.inghub.loanapi.repository.CustomerRepository;
import com.inghub.loanapi.repository.LoanRepository;
import com.inghub.loanapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(3) // Ensures this runs after AdminSeeder
public class CustomerAndLoanSeeder implements ApplicationListener<ContextRefreshedEvent> {

    private final CustomerRepository customerRepository;
    private final LoanRepository loanRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("Initializing mock data...");
        createMockCustomersAndLoans();
        log.info("Mock data initialized.");
    }

    private void createMockCustomersAndLoans() {
        if (customerRepository.count() == 0) {
            Optional<User> adminUser = userRepository.findByEmail("a@a.com");
            Optional<User> regularUser = userRepository.findByEmail("u@a.com");

            if (adminUser.isEmpty() || regularUser.isEmpty()) {
                log.error("Admin or Regular User not found. Mock data initialization aborted.");
                return;
            }

            Customer customer1 = new Customer();
            customer1.setName("John");
            customer1.setSurname("Doe");
            customer1.setCreditLimit(new BigDecimal("10000.00"));
            customer1.setUsedCreditLimit(BigDecimal.ZERO);
            customer1.setUser(adminUser.get()); // Assigning admin user

            Customer customer2 = new Customer();
            customer2.setName("Jane");
            customer2.setSurname("Smith");
            customer2.setCreditLimit(new BigDecimal("15000.00"));
            customer2.setUsedCreditLimit(BigDecimal.ZERO);
            customer2.setUser(regularUser.get()); // Assigning regular user

            customerRepository.saveAll(List.of(customer1, customer2));
            createLoanForCustomer(customer1, new BigDecimal("5000.00"), 0.15, 12);
            createLoanForCustomer(customer2, new BigDecimal("8000.00"), 0.12, 24);
        }
    }

    private void createLoanForCustomer(Customer customer, BigDecimal amount, double interestRate, int installments) {
        validateLoanParameters(customer, amount, interestRate, installments);

        BigDecimal totalAmount = calculateTotalLoanAmount(amount, interestRate);
        Loan loan = new Loan();
        loan.setCustomer(customer);
        loan.setLoanAmount(amount);
        loan.setInterestRate(BigDecimal.valueOf(interestRate));
        loan.setTotalAmount(totalAmount);
        loan.setNumberOfInstallments(installments);
        loan.setIsPaid(false);
        loan.setStatus(LoanStatus.ACTIVE);

        // Generate Installments
        generateInstallments(loan, totalAmount, installments);
        customer.setUsedCreditLimit(customer.getUsedCreditLimit().add(totalAmount));
        customerRepository.save(customer);
        loanRepository.save(loan);
    }

    private void validateLoanParameters(Customer customer, BigDecimal amount, double interestRate, int installments) {
        List<Integer> allowedInstallments = List.of(6, 9, 12, 24);
        if (!allowedInstallments.contains(installments)) {
            throw new IllegalArgumentException("Number of installments can only be 6, 9, 12, or 24.");
        }

        if (interestRate < 0.1 || interestRate > 0.5) {
            throw new IllegalArgumentException("Interest rate must be between 0.1 and 0.5.");
        }

        if (customer.getUsedCreditLimit().add(amount).compareTo(customer.getCreditLimit()) > 0) {
            throw new IllegalArgumentException("Customer does not have enough credit limit for this loan.");
        }
    }

    private BigDecimal calculateTotalLoanAmount(BigDecimal amount, double interestRate) {
        return amount.multiply(BigDecimal.valueOf(1 + interestRate)).setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    private void generateInstallments(Loan loan, BigDecimal totalAmount, int installments) {
        LocalDate firstDueDate = LocalDate.now().plusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
        BigDecimal installmentAmount = totalAmount.divide(BigDecimal.valueOf(installments), 2, BigDecimal.ROUND_HALF_UP);

        for (int i = 0; i < installments; i++) {
            LoanInstallment installment = new LoanInstallment();
            installment.setLoan(loan);
            installment.setInstallmentNumber(i + 1);
            installment.setIsPaid(false);
            installment.setStatus(InstallmentStatus.PENDING);
            installment.setDueDate(firstDueDate.plusMonths(i));
            installment.setAmount(installmentAmount);
            loan.getInstallments().add(installment);
        }
    }
}

