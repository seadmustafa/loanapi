package com.inghub.loanapi.service;

import com.inghub.loanapi.dto.CustomerRequest;
import com.inghub.loanapi.dto.CustomerResponse;
import com.inghub.loanapi.dto.LoanResponse;
import com.inghub.loanapi.dto.mapper.CustomerMapper;
import com.inghub.loanapi.entity.Customer;
import com.inghub.loanapi.entity.Role;
import com.inghub.loanapi.entity.User;
import com.inghub.loanapi.enums.RoleEnum;
import com.inghub.loanapi.exception.CustomerNotFoundException;
import com.inghub.loanapi.repository.CustomerRepository;
import com.inghub.loanapi.repository.UserRepository;
import com.inghub.loanapi.service.impl.CustomerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private UserRepository userRepository;
    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerServiceImpl customerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should Retrieve Customer by ID Successfully")
    void shouldRetrieveCustomerById() {
        // Arrange
        Customer customer = buildCustomer(1L, "John", "Doe", BigDecimal.valueOf(5000));
        CustomerResponse response = buildCustomerResponse(1L, "John", "Doe", BigDecimal.valueOf(5000), Collections.emptyList());

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerMapper.toResponse(customer)).thenReturn(response);

        // Act
        CustomerResponse result = customerService.getCustomerById(1L);

        // Assert
        assertThat(result)
                .isNotNull()
                .extracting(CustomerResponse::getName, CustomerResponse::getCreditLimit)
                .containsExactly("John", BigDecimal.valueOf(5000));

        verify(customerRepository).findById(1L);
        verify(customerMapper).toResponse(customer);
    }

    @Test
    @DisplayName("Should Throw Exception When Customer Not Found")
    void shouldThrowExceptionWhenCustomerNotFound() {
        // Arrange
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> customerService.getCustomerById(1L))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessage("Customer not found with ID: 1");

        verify(customerRepository).findById(1L);
        verifyNoInteractions(customerMapper);
    }

    @Test
    @DisplayName("Should Create Customer Successfully")
    void shouldCreateCustomerSuccessfully() {
        // Arrange
        CustomerRequest request = new CustomerRequest("Jane", "Smith", BigDecimal.valueOf(3000), RoleEnum.ADMIN);
        Customer customer = buildCustomer(null, "Jane", "Smith", BigDecimal.valueOf(3000));
        Customer savedCustomer = buildCustomer(1L, "Jane", "Smith", BigDecimal.valueOf(3000));
        CustomerResponse response = buildCustomerResponse(1L, "Jane", "Smith", BigDecimal.valueOf(3000), Collections.emptyList());
        Role adminRole = new Role();
        adminRole.setName(RoleEnum.ADMIN);
        User adminUser = new User();
        adminUser.setId(1);
        adminUser.setFullName("Super Admin");
        adminUser.setEmail("a@a.com");
        adminUser.setRole(adminRole);

        // Mocking the user lookup by ID (for ADMIN)
        when(userRepository.findById(1)).thenReturn(Optional.of(adminUser));

        // Mocking the customer creation process
        when(customerMapper.toEntity(request)).thenReturn(customer);
        when(customerRepository.save(customer)).thenReturn(savedCustomer);
        when(customerMapper.toResponse(savedCustomer)).thenReturn(response);

        // Act
        CustomerResponse result = customerService.createCustomer(request);

        // Assert
        assertThat(result)
                .isNotNull()
                .extracting(CustomerResponse::getName, CustomerResponse::getCreditLimit)
                .containsExactly("Jane", BigDecimal.valueOf(3000));

        verify(customerRepository).save(customer);
        verify(customerMapper).toResponse(savedCustomer);
        verify(userRepository).findById(1); // Ensure user lookup is verified
    }

    @Test
    @DisplayName("Should Update Customer Successfully")
    void shouldUpdateCustomerSuccessfully() {
        // Arrange
        Customer existingCustomer = buildCustomer(1L, "Old Name", "Old Surname", BigDecimal.valueOf(2000));
        CustomerRequest request = new CustomerRequest("New Name", "New Surname", BigDecimal.valueOf(5000), RoleEnum.ADMIN);
        CustomerResponse response = buildCustomerResponse(1L, "New Name", "New Surname", BigDecimal.valueOf(5000), Collections.emptyList());

        when(customerRepository.findById(1L)).thenReturn(Optional.of(existingCustomer));
        when(customerMapper.toResponse(existingCustomer)).thenReturn(response);

        // Act
        CustomerResponse result = customerService.updateCustomer(1L, request);

        // Assert
        assertThat(result)
                .isNotNull()
                .extracting(CustomerResponse::getName, CustomerResponse::getCreditLimit)
                .containsExactly("New Name", BigDecimal.valueOf(5000));

        verify(customerRepository).findById(1L);
        verify(customerMapper).toResponse(existingCustomer);
    }

    @Test
    @DisplayName("Should List All Customers Successfully")
    void shouldListAllCustomers() {
        // Arrange
        Customer customer1 = buildCustomer(1L, "John", "Doe", BigDecimal.valueOf(5000));
        Customer customer2 = buildCustomer(2L, "Jane", "Smith", BigDecimal.valueOf(3000));
        CustomerResponse response1 = buildCustomerResponse(1L, "John", "Doe", BigDecimal.valueOf(5000), Collections.emptyList());
        CustomerResponse response2 = buildCustomerResponse(2L, "Jane", "Smith", BigDecimal.valueOf(3000), Collections.emptyList());

        when(customerRepository.findAll()).thenReturn(List.of(customer1, customer2));
        when(customerMapper.toResponse(customer1)).thenReturn(response1);
        when(customerMapper.toResponse(customer2)).thenReturn(response2);

        // Act
        List<CustomerResponse> result = customerService.listAllCustomers();

        // Assert
        assertThat(result)
                .hasSize(2)
                .extracting(CustomerResponse::getName)
                .containsExactly("John", "Jane");

        verify(customerRepository).findAll();
        verify(customerMapper, times(2)).toResponse(any(Customer.class));
    }

    // Helper Methods for Clean Test Setup
    private Customer buildCustomer(Long id, String name, String surname, BigDecimal creditLimit) {
        Customer customer = new Customer();
        customer.setId(id);
        customer.setName(name);
        customer.setSurname(surname);
        customer.setCreditLimit(creditLimit);
        customer.setUsedCreditLimit(BigDecimal.ZERO);
        return customer;
    }

    private CustomerResponse buildCustomerResponse(Long id, String name, String surname, BigDecimal creditLimit, List<LoanResponse> loanResponseList) {
        return new CustomerResponse(id, name, surname, creditLimit, BigDecimal.ZERO, loanResponseList);
    }
}
