package com.inghub.loanapi.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.inghub.loanapi.dto.LoginUserDto;
import com.inghub.loanapi.entity.User;
import com.inghub.loanapi.service.AuthenticationService;
import com.inghub.loanapi.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private AuthenticationController authenticationController;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationService authenticationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authenticationController).build();
    }

    @Test
    @DisplayName("Should Authenticate User and Return JWT Token Successfully")
    void shouldAuthenticateAndReturnJwtToken() throws Exception {
        // Arrange
        LoginUserDto loginUserDto = new LoginUserDto();
        loginUserDto.setEmail("user@example.com");
        loginUserDto.setPassword("password");

        User authenticatedUser = new User().setEmail("user@example.com");
        String jwtToken = "mock-jwt-token";

        when(authenticationService.authenticate(any(LoginUserDto.class))).thenReturn(authenticatedUser);
        when(jwtService.generateToken(authenticatedUser)).thenReturn(jwtToken);
        when(jwtService.getExpirationTime()).thenReturn(3600000L); // 1 hour

        // Act & Assert
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginUserDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(jwtToken))
                .andExpect(jsonPath("$.expiresIn").value(3600000L));

        verify(authenticationService).authenticate(any(LoginUserDto.class));
        verify(jwtService).generateToken(authenticatedUser);
    }
}
