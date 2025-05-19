package com.inghub.loanapi.dto;

import com.inghub.loanapi.enums.RoleEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRequest {

    @NotBlank(message = "Customer name cannot be empty")
    private String name;

    @NotBlank(message = "Customer surname cannot be empty")
    private String surname;

    @NotNull(message = "Credit limit cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Credit limit must be greater than zero")
    @Digits(integer = 18, fraction = 2, message = "Credit limit must be a valid decimal number with two decimal places")
    private BigDecimal creditLimit;

    @NotNull(message = "Role is required")
    @Schema(description = "Role of the customer",
            allowableValues = {"ADMIN", "USER"})
    private RoleEnum role;

}
