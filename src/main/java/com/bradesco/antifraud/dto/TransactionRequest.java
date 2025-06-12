package com.bradesco.antifraud.dto;

import com.bradesco.antifraud.model.Transaction;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
// Não precisa importar UUID aqui se os campos são Strings

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {

    @NotNull(message = "O tipo da transação é obrigatório.")
    private Transaction.TransactionType tipo;

    @NotNull(message = "O valor da transação é obrigatório.")
    @DecimalMin(value = "0.01", message = "O valor da transação deve ser maior que zero.")
    private BigDecimal valor;

    @NotNull(message = "A data e hora da transação são obrigatórias.")
    private LocalDateTime dataHora;

    private String descricao;

    // Estes campos agora são String para receber os UUIDs das contas no JSON
    private String contaDeOrigem;
    private String contaDeDestino;
}
