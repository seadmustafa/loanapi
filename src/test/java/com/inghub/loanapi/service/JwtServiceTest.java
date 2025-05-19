package com.inghub.loanapi.service;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private JwtService jwtService;

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.expiration-time}")
    private long jwtExpiration;

    private static final String TEST_USERNAME = "testuser@example.com";
    private static final String TEST_ROLE = "USER";
    private static final long TEST_EXPIRATION = 1000 * 60 * 60; // 1 hour

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        String secureBase64Key = "3cfa76ef14937c1c0ea519f8fc057a80fcd04a7420f8e8bcd0a7567c272e007="; // This is a valid 256-bit Base64 key

        ReflectionTestUtils.setField(jwtService, "secretKey", secureBase64Key);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", TEST_EXPIRATION);

        userDetails = User.builder()
                .username(TEST_USERNAME)
                .password("password")
                .roles(TEST_ROLE)
                .build();
    }

    @Test
    @DisplayName("Should Generate JWT Token Successfully")
    void shouldGenerateTokenSuccessfully() {
        // Act
        String token = jwtService.generateToken(userDetails);

        // Assert
        assertThat(token).isNotNull();
        assertThat(jwtService.extractUsername(token)).isEqualTo(TEST_USERNAME);
    }

    @Test
    @DisplayName("Should Extract Username Successfully")
    void shouldExtractUsernameSuccessfully() {
        // Arrange
        String token = jwtService.generateToken(userDetails);

        // Act
        String extractedUsername = jwtService.extractUsername(token);

        // Assert
        assertThat(extractedUsername).isEqualTo(TEST_USERNAME);
    }

    @Test
    @DisplayName("Should Validate Token Successfully")
    void shouldValidateTokenSuccessfully() {
        // Arrange
        String token = jwtService.generateToken(userDetails);

        // Act
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        // Assert
        assertThat(isValid).isTrue();
    }


    @Test
    @DisplayName("Should Return False for Token with Different Username")
    void shouldReturnFalseForInvalidUsernameToken() {
        // Arrange
        UserDetails otherUser = User.builder().username("otheruser@example.com").password("password").roles(TEST_ROLE).build();
        String token = jwtService.generateToken(userDetails);

        // Act
        boolean isValid = jwtService.isTokenValid(token, otherUser);

        // Assert
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should Extract Claim Successfully")
    void shouldExtractClaimSuccessfully() {
        // Arrange
        String token = jwtService.generateToken(userDetails);

        // Act
        Date expirationDate = jwtService.extractClaim(token, Claims::getExpiration);

        // Assert
        assertThat(expirationDate).isNotNull();
        assertThat(expirationDate).isAfter(new Date());
    }

    @Test
    @DisplayName("Should Generate Token with Custom Claims Successfully")
    void shouldGenerateTokenWithCustomClaimsSuccessfully() {
        // Arrange
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("customClaim", "customValue");

        // Act
        String token = jwtService.generateToken(extraClaims, userDetails);
        Claims claims = jwtService.extractAllClaims(token);

        // Assert
        assertThat(claims.get("customClaim")).isEqualTo("customValue");
        assertThat(claims.getSubject()).isEqualTo(TEST_USERNAME);
    }

    @Test
    @DisplayName("Should Extract All Claims Successfully")
    void shouldExtractAllClaimsSuccessfully() {
        // Arrange
        String token = jwtService.generateToken(userDetails);

        // Act
        Claims claims = jwtService.extractAllClaims(token);

        // Assert
        assertThat(claims).isNotNull();
        assertThat(claims.getSubject()).isEqualTo(TEST_USERNAME);
    }


    @Test
    @DisplayName("Should Handle Malformed Token Gracefully")
    void shouldHandleMalformedTokenGracefully() {
        // Arrange
        String malformedToken = "malformed.token";

        // Act & Assert
        assertThatThrownBy(() -> jwtService.extractAllClaims(malformedToken))
                .isInstanceOf(MalformedJwtException.class);
    }


}
