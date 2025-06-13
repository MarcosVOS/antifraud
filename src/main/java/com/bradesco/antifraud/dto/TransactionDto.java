package com.bradesco.antifraud.dto;

import com.bradesco.antifraud.model.Transaction;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.Value;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for {@link com.bradesco.antifraud.model.Transaction}
 */

@Builder
@Data
public class TransactionDto implements Serializable {
   private UUID id;

   private  Transaction.TransactionType tipo;

   @NotNull
   private  BigDecimal valor;
   @NotNull
   private  LocalDateTime dataHora;
   private  String descricao;
   private  UUID contaDeOrigemId;
   private  UUID contaDeDestinoId;
}