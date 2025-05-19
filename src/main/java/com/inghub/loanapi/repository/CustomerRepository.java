package com.inghub.loanapi.repository;

import com.inghub.loanapi.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {


    Optional<Customer> findById(Long customerId);

    @Modifying
    @Transactional
    @Query("UPDATE Customer c SET c.usedCreditLimit = :usedCredit WHERE c.id = :customerId")
    void updateUsedCreditLimit(@Param("customerId") Long customerId, @Param("usedCredit") BigDecimal usedCredit);
}
