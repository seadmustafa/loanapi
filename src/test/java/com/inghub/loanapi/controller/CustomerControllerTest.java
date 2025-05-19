package com.inghub.loanapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inghub.loanapi.dto.CustomerRequest;
import com.inghub.loanapi.dto.CustomerResponse;
import com.inghub.loanapi.enums.RoleEnum;
import com.inghub.loanapi.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CustomerControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private CustomerController customerController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(customerController).build();
    }

    @Test
    @DisplayName("Should Retrieve Customer by ID Successfully")
    void shouldRetrieveCustomerById() throws Exception {
        // Arrange
        CustomerResponse response = new CustomerResponse(1L, "John", "Doe", BigDecimal.valueOf(5000), BigDecimal.ZERO, List.of());

        when(customerService.getCustomerById(1L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.surname").value("Doe"))
                .andExpect(jsonPath("$.creditLimit").value(5000));

        verify(customerService).getCustomerById(1L);
    }

    @Test
    @DisplayName("Should Create Customer Successfully")
    void shouldCreateCustomerSuccessfully() throws Exception {
        // Arrange
        CustomerRequest request = new CustomerRequest("Jane", "Smith", BigDecimal.valueOf(3000), RoleEnum.ADMIN);
        CustomerResponse response = new CustomerResponse(1L, "Jane", "Smith", BigDecimal.valueOf(3000), BigDecimal.ZERO, List.of());

        when(customerService.createCustomer(any(CustomerRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Jane"))
                .andExpect(jsonPath("$.surname").value("Smith"))
                .andExpect(jsonPath("$.creditLimit").value(3000));

        verify(customerService).createCustomer(any(CustomerRequest.class));
    }

    @Test
    @DisplayName("Should Update Customer Successfully")
    void shouldUpdateCustomerSuccessfully() throws Exception {
        // Arrange
        CustomerRequest request = new CustomerRequest("John", "Updated", BigDecimal.valueOf(4000), RoleEnum.ADMIN);
        CustomerResponse response = new CustomerResponse(1L, "John", "Updated", BigDecimal.valueOf(4000), BigDecimal.ZERO, List.of());

        when(customerService.updateCustomer(eq(1L), any(CustomerRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.surname").value("Updated"))
                .andExpect(jsonPath("$.creditLimit").value(4000));

        verify(customerService).updateCustomer(eq(1L), any(CustomerRequest.class));
    }

    @Test
    @DisplayName("Should List All Customers Successfully")
    void shouldListAllCustomersSuccessfully() throws Exception {
        // Arrange
        CustomerResponse customer1 = new CustomerResponse(1L, "John", "Doe", BigDecimal.valueOf(5000), BigDecimal.ZERO, List.of());
        CustomerResponse customer2 = new CustomerResponse(2L, "Jane", "Smith", BigDecimal.valueOf(3000), BigDecimal.ZERO, List.of());

        when(customerService.listAllCustomers()).thenReturn(List.of(customer1, customer2));

        // Act & Assert
        mockMvc.perform(get("/api/customers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("John"))
                .andExpect(jsonPath("$[1].name").value("Jane"));

        verify(customerService).listAllCustomers();
    }
}

