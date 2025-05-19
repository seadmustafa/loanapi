package com.inghub.loanapi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"loans"})
@EqualsAndHashCode(of = {"id"})
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(nullable = false)
    private String surname;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false, message = "Credit limit must be positive")
    @Column(nullable = false)
    private BigDecimal creditLimit;

    @NotNull
    @DecimalMin(value = "0.0", message = "Used credit limit cannot be negative")
    @Column(nullable = false)
    private BigDecimal usedCreditLimit = BigDecimal.ZERO;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Loan> loans = new HashSet<>(); // Initialize collections

    @OneToOne
    private User user;
}