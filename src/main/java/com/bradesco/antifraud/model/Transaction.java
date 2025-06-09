package com.bradesco.antifraud.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;

import lombok.Builder; 
import lombok.Data;    

@Entity
@Data 
@Builder 
public class Transaction {

    @Id
    @GeneratedValue
    private UUID id;

    public enum TransactionType{
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
    private String descricao;

    @ManyToOne
    private Account contaDeOrigem;
    @ManyToOne
    private Account contaDeDestino;

}
