package com.inghub.loanapi.dto;


import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanRequest {

    @NotNull(message = "Customer ID cannot be null")
    private Long customerId;

    @NotNull(message = "Loan amount cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Loan amount must be greater than zero")
    @Digits(integer = 18, fraction = 2, message = "Loan amount must be a valid decimal number with two decimal places")
    private BigDecimal amount;

    @NotNull(message = "Interest rate cannot be null")
    @DecimalMin(value = "0.1", message = "Interest rate must be at least 0.1")
    @DecimalMax(value = "0.5", message = "Interest rate must be at most 0.5")
    @Digits(integer = 1, fraction = 3, message = "Interest rate must be a valid decimal number with three decimal places")
    private BigDecimal interestRate;

    @NotNull(message = "Number of installments cannot be null")
    private Integer numberOfInstallments;


}
