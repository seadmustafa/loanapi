package com.inghub.loanapi.entity;

import com.inghub.loanapi.enums.InstallmentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "loan_installments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"loan"}) // Exclude to prevent recursion
@EqualsAndHashCode(of = {"id"})
public class LoanInstallment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @NotNull
    @Column(nullable = false)
    private Integer installmentNumber;

    @NotNull
    @Column(nullable = false)
    private BigDecimal amount;

    @Column(precision = 19, scale = 2)
    private BigDecimal paidAmount;

    @NotNull
    @Column(nullable = false)
    private LocalDate dueDate;

    //    @NotNull
    private LocalDate paymentDate;

    @NotNull
    @Column(nullable = false)
    private Boolean isPaid = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InstallmentStatus status;

}