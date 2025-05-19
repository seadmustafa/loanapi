package com.inghub.loanapi.controller;

import com.inghub.loanapi.dto.CustomerRequest;
import com.inghub.loanapi.dto.CustomerResponse;
import com.inghub.loanapi.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @Operation(summary = "Get customer by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer found"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and #customerId == authentication.principal.id)")
    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Long customerId) {
        return ResponseEntity.ok(customerService.getCustomerById(customerId));
    }

    @Operation(summary = "Create a new customer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Customer created successfully")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(@RequestBody @Valid CustomerRequest request) {
        return ResponseEntity.status(201).body(customerService.createCustomer(request));
    }


    @Operation(summary = "Update customer details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Customer updated successfully"),
            @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @PutMapping("/{customerId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and #customerId == authentication.principal.id)")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable Long customerId,
            @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(customerService.updateCustomer(customerId, request));
    }

    @Operation(summary = "List all customers")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of customers retrieved successfully")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<CustomerResponse>> listCustomers() {
        return ResponseEntity.ok(customerService.listAllCustomers());
    }
}
