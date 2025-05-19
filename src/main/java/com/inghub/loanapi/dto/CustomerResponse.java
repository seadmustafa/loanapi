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
public class CustomerResponse {

    private Long customerId;
    private String name;
    private String surname;
    private BigDecimal creditLimit;
    private BigDecimal usedCreditLimit;
    private List<LoanResponse> loans;
}
