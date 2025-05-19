package com.inghub.loanapi.service;

import com.inghub.loanapi.dto.CustomerRequest;
import com.inghub.loanapi.dto.CustomerResponse;

import java.util.List;

public interface CustomerService {
    CustomerResponse getCustomerById(Long customerId);

    CustomerResponse createCustomer(CustomerRequest request);

    CustomerResponse updateCustomer(Long customerId, CustomerRequest request);

    List<CustomerResponse> listAllCustomers();
}
