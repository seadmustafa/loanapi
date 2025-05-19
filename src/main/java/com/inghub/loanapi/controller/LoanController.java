package com.inghub.loanapi.controller;


import com.inghub.loanapi.dto.*;
import com.inghub.loanapi.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@Validated
public class LoanController {

    private final LoanService loanService;

    /**
     * Create a new loan for a customer.
     */
    @Operation(summary = "Create a new loan for a customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Loan created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "404", description = "Customer not found"),
            @ApiResponse(responseCode = "409", description = "Insufficient credit limit")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<LoanResponse> createLoan(@Valid @RequestBody LoanRequest loanRequest) {
        log.info("Received request to create loan for customerId: {}", loanRequest.getCustomerId());
        LoanResponse response = loanService.createLoan(loanRequest);
        log.info("Loan created successfully with loanId: {}", response.getLoanId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * List all loans for a customer with optional filters.
     */
    @Operation(summary = "List all loans for a customer with optional filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Loans listed successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and #customerId == authentication.principal.id)")
    @GetMapping
    public ResponseEntity<List<LoanResponse>> listLoans(
            @RequestParam Long customerId,
            @RequestParam(required = false) Integer numInstallments,
            @RequestParam(required = false) Boolean isPaid) {
        log.info("Listing loans for customerId: {}, numInstallments: {}, isPaid: {}", customerId, numInstallments, isPaid);
        List<LoanResponse> loans = loanService.listLoans(customerId, numInstallments, isPaid);
        log.info("Found {} loans for customerId: {}", loans.size(), customerId);
        return ResponseEntity.ok(loans);
    }

    /**
     * List all installments for a specific loan.
     */
    @Operation(summary = "List all installments for a specific loan")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Installments listed successfully"),
            @ApiResponse(responseCode = "404", description = "Loan not found")
    })
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and #customerId == authentication.principal.id)")
    @GetMapping("/{loanId}/installments")
    public ResponseEntity<List<InstallmentResponse>> listInstallments(@PathVariable Long loanId) {
        log.info("Listing installments for loanId: {}", loanId);
        List<InstallmentResponse> installments = loanService.listInstallmentsForLoan(loanId);
        log.info("Found {} installments for loanId: {}", installments.size(), loanId);
        return ResponseEntity.ok(installments);
    }


    /**
     * Pay one or more installments for a loan.
     */
    @Operation(summary = "Pay one or more installments for a loan")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Installments paid successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data or payment amount"),
            @ApiResponse(responseCode = "404", description = "Loan not found"),
            @ApiResponse(responseCode = "409", description = "Loan already fully paid")
    })
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and #customerId == authentication.principal.id)")
    @PostMapping("/{loanId}/pay")
    public ResponseEntity<LoanPaymentResponse> payLoan(
            @PathVariable Long loanId,
            @Valid @RequestBody LoanPaymentRequest paymentRequest) {
        log.info("Processing payment for loanId: {}", loanId);
        if (!loanId.equals(paymentRequest.getLoanId())) {
            log.error("Mismatch between path loanId and request loanId");
            return ResponseEntity.badRequest().body(null);
        }

        LoanPaymentResponse response = loanService.payLoanInstallment(paymentRequest);
        log.info("Payment processed for loanId: {}. Total paid: {}", response.getLoanId(), response.getTotalPaid());
        return ResponseEntity.ok(response);
    }
}
