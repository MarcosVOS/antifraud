package com.bradesco.antifraud.dto;

import com.bradesco.antifraud.model.Transaction; // Importa a enum TransactionType
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor 
public class TransactionDTO {

    private UUID id; 

    @NotNull(message = "O tipo da transação é obrigatório.")
    private Transaction.TransactionType tipo; 

    @NotNull(message = "O valor da transação é obrigatório.")
    @DecimalMin(value = "0.01", message = "O valor da transação deve ser maior que zero.")
    private BigDecimal valor;

    @NotNull(message = "A data e hora da transação são obrigatórias.")
    private LocalDateTime dataHora;

    private String descricao;
    private UUID contaDeOrigem;
    private UUID contaDeDestino;
}