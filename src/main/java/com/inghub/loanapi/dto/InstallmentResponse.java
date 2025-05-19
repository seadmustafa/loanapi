package com.inghub.loanapi.dto;


import com.inghub.loanapi.enums.InstallmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstallmentResponse {

    private Long installmentId;
    private Long loanId;
    private Integer installmentNumber;
    private BigDecimal amount;
    private BigDecimal paidAmount;
    private LocalDate dueDate;
    private LocalDate paymentDate;
    private Boolean isPaid;
    private InstallmentStatus status;


}
