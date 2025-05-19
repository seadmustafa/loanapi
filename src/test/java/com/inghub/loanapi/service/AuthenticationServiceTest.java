package com.inghub.loanapi.service;

import com.inghub.loanapi.dto.LoginUserDto;
import com.inghub.loanapi.entity.User;
import com.inghub.loanapi.repository.RoleRepository;
import com.inghub.loanapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Captor
    private ArgumentCaptor<UsernamePasswordAuthenticationToken> authenticationTokenCaptor;

    private LoginUserDto loginUserDto;

    @BeforeEach
    void setUp() {
        loginUserDto = new LoginUserDto();
        loginUserDto.setEmail("user@example.com");
        loginUserDto.setPassword("password");
    }

    @Test
    @DisplayName("Should Authenticate User Successfully")
    void shouldAuthenticateUserSuccessfully() {
        // Arrange
        User user = new User().setEmail("user@example.com");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("user@example.com", "password"));
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        // Act
        User result = authenticationService.authenticate(loginUserDto);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("user@example.com");

        verify(authenticationManager).authenticate(authenticationTokenCaptor.capture());
        verify(userRepository).findByEmail("user@example.com");

        UsernamePasswordAuthenticationToken token = authenticationTokenCaptor.getValue();
        assertThat(token.getPrincipal()).isEqualTo("user@example.com");
        assertThat(token.getCredentials()).isEqualTo("password");
    }

    @Test
    @DisplayName("Should Throw Exception for Invalid Credentials")
    void shouldThrowExceptionForInvalidCredentials() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        assertThatThrownBy(() -> authenticationService.authenticate(loginUserDto))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("Invalid credentials");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    @DisplayName("Should Throw Exception When User Not Found")
    void shouldThrowExceptionWhenUserNotFound() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("user@example.com", "password"));
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authenticationService.authenticate(loginUserDto))
                .isInstanceOf(RuntimeException.class);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("user@example.com");
    }
}
