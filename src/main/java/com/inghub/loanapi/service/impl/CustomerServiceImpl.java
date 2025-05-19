package com.inghub.loanapi.service.impl;

import com.inghub.loanapi.dto.CustomerRequest;
import com.inghub.loanapi.dto.CustomerResponse;
import com.inghub.loanapi.dto.mapper.CustomerMapper;
import com.inghub.loanapi.entity.Customer;
import com.inghub.loanapi.entity.User;
import com.inghub.loanapi.enums.RoleEnum;
import com.inghub.loanapi.exception.CustomerNotFoundException;
import com.inghub.loanapi.repository.CustomerRepository;
import com.inghub.loanapi.repository.UserRepository;
import com.inghub.loanapi.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final CustomerMapper customerMapper;

    @Override
    public CustomerResponse getCustomerById(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));
        return customerMapper.toResponse(customer);
    }

    @Override
    public CustomerResponse createCustomer(CustomerRequest request) {
        Integer userId = determineUserIdByRole(request.getRole());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        Customer customer = customerMapper.toEntity(request);
        customer.setUser(user); // Associate the user with the customer
        customer.setUsedCreditLimit(BigDecimal.ZERO);

        Customer savedCustomer = customerRepository.save(customer);
        return customerMapper.toResponse(savedCustomer);
    }

    private Integer determineUserIdByRole(RoleEnum role) {
        return switch (role) {
            case ADMIN -> 1;
            case CUSTOMER -> 2;
            default -> throw new IllegalArgumentException("Invalid role specified. Allowed roles: ADMIN, USER");
        };
    }

    @Override
    public CustomerResponse updateCustomer(Long customerId, CustomerRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));

        customer.setName(request.getName());
        customer.setSurname(request.getSurname());
        customer.setCreditLimit(request.getCreditLimit());
        return customerMapper.toResponse(customer);
    }


    @Override
    public List<CustomerResponse> listAllCustomers() {
        return customerRepository.findAll().stream()
                .map(customerMapper::toResponse)
                .collect(Collectors.toList());
    }
}
