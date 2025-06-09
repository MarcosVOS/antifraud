package com.bradesco.antifraud.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotNull
    @Column(name = "source_account_id")
    private UUID sourceAccountId;

    @NotNull
    @Column(name = "destination_account_id")
    private UUID destinationAccountId;

    @NotNull
    private BigDecimal amount;

    @NotNull
    private LocalDateTime transactionDate;

    @Enumerated(EnumType.STRING)
    @NotNull
    private TransactionStatus status; 

    public enum TransactionStatus {PENDING, APPROVED, REJECTED, CANCELED}
}
