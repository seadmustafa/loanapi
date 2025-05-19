package com.inghub.loanapi.dto;

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
public class LoanPaymentRequest {

    @NotNull(message = "Loan ID cannot be null")
    private Long loanId;

    @NotNull(message = "Payment amount cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Payment amount must be greater than zero")
    @Digits(integer = 18, fraction = 2, message = "Payment amount must be a valid decimal number with two decimal places")
    private BigDecimal paymentAmount;

}
