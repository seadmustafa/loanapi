package com.inghub.loanapi.service;

import com.inghub.loanapi.dto.*;

import java.util.List;

public interface LoanService {
    LoanResponse createLoan(LoanRequest createLoanRequest);

    List<LoanResponse> listLoans(Long customerId, Integer numInstallments, Boolean isPaid);

    List<InstallmentResponse> listInstallmentsForLoan(Long loanId);

    LoanPaymentResponse payLoanInstallment(LoanPaymentRequest paymentRequest);
}
