package com.inghub.loanapi.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanResponse {

    private Long loanId;
    private Long customerId;
    private BigDecimal loanAmount;
    private BigDecimal totalAmount;
    private BigDecimal interestRate;
    private Integer numberOfInstallments;
    private LocalDateTime createDate;
    private Boolean isPaid;
    private String status;
    private List<InstallmentResponse> installments;
}
