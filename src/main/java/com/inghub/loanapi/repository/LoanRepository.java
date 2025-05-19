package com.inghub.loanapi.repository;

import com.inghub.loanapi.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    // List loans for a given customer
    List<Loan> findByCustomerId(Long customerId);

}
