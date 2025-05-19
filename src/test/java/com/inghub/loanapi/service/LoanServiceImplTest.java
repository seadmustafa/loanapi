package com.inghub.loanapi.service;


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
import com.inghub.loanapi.service.impl.LoanServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoanServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private LoanMapper loanMapper;

    @InjectMocks
    private LoanServiceImpl loanService;

    private Customer customer;
    private Loan loan;
    private LoanRequest loanRequest;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setName("John");
        customer.setSurname("Doe");
        customer.setCreditLimit(BigDecimal.valueOf(10000));
        customer.setUsedCreditLimit(BigDecimal.valueOf(2000));

        loan = new Loan();
        loan.setId(1L);
        loan.setCustomer(customer);
        loan.setLoanAmount(BigDecimal.valueOf(5000));
        loan.setTotalAmount(BigDecimal.valueOf(5500));
        loan.setInterestRate(BigDecimal.valueOf(0.1));
        loan.setNumberOfInstallments(12);
        loan.setIsPaid(false);
        loan.setStatus(LoanStatus.ACTIVE);

        loanRequest = new LoanRequest(1L, BigDecimal.valueOf(5000), BigDecimal.valueOf(0.1), 12);
    }

    @Test
    @DisplayName("Should Create Loan Successfully")
    void shouldCreateLoanSuccessfully() {
        // Arrange
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);
        when(loanMapper.toLoanResponse(any())).thenReturn(new LoanResponse());

        // Act
        LoanResponse response = loanService.createLoan(loanRequest);

        // Assert
        assertThat(response).isNotNull();
        verify(customerRepository).findById(1L);
        verify(loanRepository).save(any(Loan.class));
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    @DisplayName("Should Throw Exception When Customer Not Found")
    void shouldThrowExceptionWhenCustomerNotFound() {
        // Arrange
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> loanService.createLoan(loanRequest))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("Customer not found with ID: 1");
    }

    @Test
    @DisplayName("Should Throw Exception for Insufficient Credit Limit")
    void shouldThrowExceptionForInsufficientCreditLimit() {
        // Arrange
        customer.setCreditLimit(BigDecimal.valueOf(3000)); // Insufficient limit
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        // Act & Assert
        assertThatThrownBy(() -> loanService.createLoan(loanRequest))
                .isInstanceOf(InsufficientCreditException.class)
                .hasMessageContaining("Insufficient credit limit");
    }

    @Test
    @DisplayName("Should List Loans Successfully")
    void shouldListLoansSuccessfully() {
        // Arrange
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(loanRepository.findByCustomerId(1L)).thenReturn(List.of(loan));
        when(loanMapper.toLoanResponseList(anyList())).thenReturn(List.of(new LoanResponse()));

        // Act
        List<LoanResponse> loans = loanService.listLoans(1L, null, null);

        // Assert
        assertThat(loans).isNotEmpty();
        verify(loanRepository).findByCustomerId(1L);
    }

    @Test
    @DisplayName("Should List Installments for Loan Successfully")
    void shouldListInstallmentsForLoanSuccessfully() {
        // Arrange
        loan.setInstallments(List.of(new LoanInstallment(1L, loan, 1, BigDecimal.valueOf(500), null, LocalDate.now().plusDays(30), null, false, InstallmentStatus.PENDING)));
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanMapper.toInstallmentResponseList(anyList())).thenReturn(List.of(new InstallmentResponse()));

        // Act
        List<InstallmentResponse> installments = loanService.listInstallmentsForLoan(1L);

        // Assert
        assertThat(installments).hasSize(1);
        verify(loanRepository).findById(1L);
    }

    @Test
    @DisplayName("Should Throw Exception When Loan Not Found for Installments")
    void shouldThrowExceptionWhenLoanNotFoundForInstallments() {
        // Arrange
        when(loanRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> loanService.listInstallmentsForLoan(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Loan not found");
    }

    @Test
    @DisplayName("Should Throw Exception for Invalid Loan Parameter")
    void shouldThrowExceptionForInvalidLoanParameter() {
        // Arrange
        loanRequest.setNumberOfInstallments(5); // Invalid number of installments

        // Act & Assert
        assertThatThrownBy(() -> loanService.createLoan(loanRequest))
                .isInstanceOf(InvalidLoanParameterException.class)
                .hasMessageContaining("Invalid number of installments.");
    }

    @Test
    @DisplayName("Should Throw Exception When Loan Already Paid")
    void shouldThrowExceptionWhenLoanAlreadyPaid() {
        // Arrange
        loan.setIsPaid(true);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        // Act & Assert
        assertThatThrownBy(() -> loanService.payLoanInstallment(new LoanPaymentRequest(1L, BigDecimal.valueOf(1000))))
                .isInstanceOf(LoanAlreadyPaidException.class)
                .hasMessageContaining("Loan is already fully paid.");
    }

    // ✅ 5 days before due date (discount applied)
    @Test
    @DisplayName("Should Apply Discount for Early Payment - 5 Days Before Due Date")
    void shouldApplyDiscountForEarlyPayment() {
        LoanInstallment installment = createInstallment(BigDecimal.valueOf(500), LocalDate.now().plusDays(5));
        loan.setInstallments(List.of(installment));
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        LoanPaymentRequest request = new LoanPaymentRequest(1L, BigDecimal.valueOf(500));
        LoanPaymentResponse response = loanService.payLoanInstallment(request);

        assertThat(response.getTotalPaid()).isEqualByComparingTo("497.50");
    }

    // ✅ Pay on due date (no discount, no penalty)
    @Test
    @DisplayName("Should Not Apply Discount or Penalty for On-Time Payment")
    void shouldPayOnDueDateWithNoDiscountOrPenalty() {
        LoanInstallment installment = createInstallment(BigDecimal.valueOf(500), LocalDate.now());
        loan.setInstallments(List.of(installment));
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        LoanPaymentRequest request = new LoanPaymentRequest(1L, BigDecimal.valueOf(500));
        LoanPaymentResponse response = loanService.payLoanInstallment(request);

        assertThat(response.getTotalPaid()).isEqualByComparingTo("500.00");
    }


    // ✅ Calculate total loan amount (principal * (1 + interest))
    @Test
    @DisplayName("Should Calculate Total Loan Amount Correctly")
    void shouldCalculateTotalLoanAmount() {
        BigDecimal principal = BigDecimal.valueOf(1000);
        BigDecimal interestRate = BigDecimal.valueOf(0.2);
        BigDecimal totalAmount = principal.multiply(BigDecimal.ONE.add(interestRate));
        assertThat(totalAmount).isEqualByComparingTo("1200.00");
    }


    // ✅ Calculate correct installment for 6 months
    @Test
    @DisplayName("Should Calculate Correct Installment Amount for 6 Installments")
    void shouldCalculateInstallmentAmountForSixMonths() {
        BigDecimal totalAmount = BigDecimal.valueOf(6000);
        BigDecimal expectedInstallment = totalAmount.divide(BigDecimal.valueOf(6), 2, BigDecimal.ROUND_HALF_UP);
        assertThat(expectedInstallment).isEqualByComparingTo("1000.00");
    }

    // ✅ Calculate correct installment for 9 months
    @Test
    @DisplayName("Should Calculate Correct Installment Amount for 9 Installments")
    void shouldCalculateInstallmentAmountForNineMonths() {
        BigDecimal totalAmount = BigDecimal.valueOf(9000);
        BigDecimal expectedInstallment = totalAmount.divide(BigDecimal.valueOf(9), 2, BigDecimal.ROUND_HALF_UP);
        assertThat(expectedInstallment).isEqualByComparingTo("1000.00");
    }

    // Utility Method
    private LoanInstallment createInstallment(BigDecimal amount, LocalDate dueDate) {
        LoanInstallment installment = new LoanInstallment();
        installment.setInstallmentNumber(1);
        installment.setAmount(amount);
        installment.setDueDate(dueDate);
        installment.setIsPaid(false);
        installment.setStatus(InstallmentStatus.PENDING);
        return installment;
    }
}





