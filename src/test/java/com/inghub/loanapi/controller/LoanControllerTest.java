package com.inghub.loanapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inghub.loanapi.dto.*;
import com.inghub.loanapi.enums.InstallmentStatus;
import com.inghub.loanapi.service.LoanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LoanControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private LoanService loanService;

    @InjectMocks
    private LoanController loanController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(loanController).build();
    }

    @Test
    @DisplayName("Should Create Loan Successfully (Admin User)")
    @WithMockUser(roles = "ADMIN")
    void shouldCreateLoanSuccessfully() throws Exception {
        // Arrange
        LocalDateTime localDateTime = LocalDate.of(2025, Month.MAY, 18).atStartOfDay();
        LocalDate today = LocalDate.now();
        InstallmentResponse installmentResponse = new InstallmentResponse(1L, 2L, 6, BigDecimal.valueOf(5000), BigDecimal.valueOf(2000), today.plusMonths(6L), today.plusMonths(2L), false, InstallmentStatus.PAID);
        List<InstallmentResponse> installmentResponseList = List.of(installmentResponse);
        LoanRequest request = new LoanRequest(1L, BigDecimal.valueOf(5000), BigDecimal.valueOf(0.1), 12);
        LoanResponse response = new LoanResponse(1L, 1L, BigDecimal.valueOf(5000), BigDecimal.valueOf(5500), BigDecimal.valueOf(0.1), 12, localDateTime, false, "ACTIVE", installmentResponseList);

        when(loanService.createLoan(any(LoanRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.loanId").value(1L))
                .andExpect(jsonPath("$.customerId").value(1L))
                .andExpect(jsonPath("$.loanAmount").value(5000));

        verify(loanService).createLoan(any(LoanRequest.class));
    }

    @Test
    @DisplayName("Should List Loans for Customer (Admin User)")
    @WithMockUser(roles = "ADMIN")
    void shouldListLoansForCustomer() throws Exception {
        // Arrange
        LocalDate today = LocalDate.now();
        InstallmentResponse installmentResponse = new InstallmentResponse(1L, 2L, 6, BigDecimal.valueOf(5000), BigDecimal.valueOf(2000), today.plusMonths(6L), today.plusMonths(2L), false, InstallmentStatus.PAID);
        List<InstallmentResponse> installmentResponseList = List.of(installmentResponse);
        LoanResponse response = new LoanResponse(1L, 1L, BigDecimal.valueOf(5000), BigDecimal.valueOf(5500), BigDecimal.valueOf(0.1), 12, null, false, "ACTIVE", installmentResponseList);
        when(loanService.listLoans(eq(1L), any(), any())).thenReturn(List.of(response));

        // Act & Assert
        mockMvc.perform(get("/api/loans")
                        .param("customerId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].loanId").value(1L));

        verify(loanService).listLoans(eq(1L), any(), any());
    }

    @Test
    @DisplayName("Should List Installments for a Loan (Admin User)")
    @WithMockUser(roles = "ADMIN")
    void shouldListInstallmentsForLoan() throws Exception {
        // Arrange
        LocalDate today = LocalDate.now();
        InstallmentResponse installment = new InstallmentResponse(1L, 1L, 1, BigDecimal.valueOf(500), BigDecimal.ZERO, today.plusMonths(6L), today.plusMonths(1L), false, InstallmentStatus.PAID);
        when(loanService.listInstallmentsForLoan(1L)).thenReturn(List.of(installment));

        // Act & Assert
        mockMvc.perform(get("/api/loans/1/installments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].installmentId").value(1L));

        verify(loanService).listInstallmentsForLoan(1L);
    }

    @Test
    @DisplayName("Should Pay Loan Installments Successfully")
    @WithMockUser(roles = "ADMIN")
    void shouldPayLoanInstallmentsSuccessfully() throws Exception {
        // Arrange
        LoanPaymentRequest paymentRequest = new LoanPaymentRequest(1L, BigDecimal.valueOf(1000));
        LoanPaymentResponse response = new LoanPaymentResponse(1L, BigDecimal.valueOf(1000), 2, false, List.of());

        when(loanService.payLoanInstallment(any(LoanPaymentRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/loans/1/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.loanId").value(1L))
                .andExpect(jsonPath("$.totalPaid").value(1000));

        verify(loanService).payLoanInstallment(any(LoanPaymentRequest.class));
    }

    @Test
    @DisplayName("Should Return Bad Request for Mismatched Loan ID in Payment")
    @WithMockUser(roles = "ADMIN")
    void shouldReturnBadRequestForMismatchedLoanId() throws Exception {
        // Arrange
        LoanPaymentRequest paymentRequest = new LoanPaymentRequest(2L, BigDecimal.valueOf(1000));

        // Act & Assert
        mockMvc.perform(post("/api/loans/1/pay")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isBadRequest());

        verify(loanService, never()).payLoanInstallment(any(LoanPaymentRequest.class));
    }
}
