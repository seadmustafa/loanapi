package com.inghub.loanapi.dto.mapper;

import com.inghub.loanapi.dto.InstallmentResponse;
import com.inghub.loanapi.dto.LoanResponse;
import com.inghub.loanapi.entity.Loan;
import com.inghub.loanapi.entity.LoanInstallment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LoanMapper {
    LoanMapper INSTANCE = Mappers.getMapper(LoanMapper.class);

    // Mapping Loan to LoanResponse
    @Mapping(source = "customer.id", target = "customerId")
    LoanResponse toLoanResponse(Loan loan);

    // Mapping List of Loans to List of LoanResponse
    List<LoanResponse> toLoanResponseList(List<Loan> loans);

    // Mapping LoanInstallment to InstallmentResponse
    @Mapping(source = "loan.id", target = "loanId")
    InstallmentResponse toInstallmentResponse(LoanInstallment installment);

    // Mapping List of LoanInstallment to List of InstallmentResponse
    List<InstallmentResponse> toInstallmentResponseList(List<LoanInstallment> installments);
}

