package com.bradesco.antifraud.model;



import jakarta.persistence.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(unique = true)
    private String accountNumber;

    @NotNull
    private String agency;

    @NotNull
    private BigDecimal balance;

    //Verificar quanto ao funcionamento do ENumType.STRING nessa situação
    @NotBlank
    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @NotBlank
    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    Customer customer;

    public enum AccountType {CORRENTE, POUPANCA, INVESTIMENTO}
    public enum AccountStatus {ATIVA, INATIVA, BLOQUADA, ENCERRADA}
}
