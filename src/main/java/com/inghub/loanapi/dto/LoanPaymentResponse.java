package com.inghub.loanapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanPaymentResponse {

    private Long loanId;
    private BigDecimal totalPaid;
    private Integer numberOfInstallmentsPaid;
    private Boolean loanFullyPaid;
    private List<InstallmentResponse> paidInstallments;
}
