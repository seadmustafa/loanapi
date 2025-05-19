package com.inghub.loanapi.entity;


import com.inghub.loanapi.enums.LoanStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "loans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"customer", "installments"}) // Exclude to prevent recursion & large output
@EqualsAndHashCode(of = {"id"})
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @NotNull
    @Column(nullable = false)
    private BigDecimal loanAmount; // Principal amount

    @NotNull
    @Column(nullable = false)
    private BigDecimal totalAmount; // loanAmount * (1 + interestRate)

    @NotNull
    @Column(nullable = false)
    private BigDecimal interestRate;

    @NotNull
    @Column(nullable = false)
    private Integer numberOfInstallments;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime createDate = LocalDateTime.now();

    @NotNull
    @Column(nullable = false)
    private Boolean isPaid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status;

    @OneToMany(mappedBy = "loan", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("installmentNumber ASC")
    private List<LoanInstallment> installments = new ArrayList<>();


}