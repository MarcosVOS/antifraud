package com.bradesco.antifraud.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn; // Adicionado import para @JoinColumn
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    public enum TransactionType {
        DEPOSITO,
        SAQUE,
        TRANSFERENCIA,
        PAGAMENTO
    }

    @NotNull
    @Enumerated(EnumType.STRING)
    private TransactionType tipo;

    @NotNull
    private BigDecimal valor;

    @NotNull
    private LocalDateTime dataHora;

    @Column(length = 255)
    private String descricao;

    @ManyToOne
    @JoinColumn(name = "source_account_id") // Nome da coluna de chave estrangeira no DB
    private Account contaDeOrigem;

    @ManyToOne
    @JoinColumn(name = "destination_account_id") // Nome da coluna de chave estrangeira no DB
    private Account contaDeDestino;
}
