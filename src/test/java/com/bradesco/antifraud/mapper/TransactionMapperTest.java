package com.bradesco.antifraud.mapper;

import com.bradesco.antifraud.dto.TransactionDTO;
import com.bradesco.antifraud.model.Account;
import com.bradesco.antifraud.model.Transaction;
import com.bradesco.antifraud.model.Transaction.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TransactionMapper Tests")
public class TransactionMapperTest {

    private final TransactionMapper mapper = Mappers.getMapper(TransactionMapper.class);

    @Test
    @DisplayName("Should map Transaction to TransactionDTO correctly")
    void shouldMapTransactionToTransactionDTO() {
        UUID transactionId = UUID.randomUUID();
        UUID sourceAccountId = UUID.randomUUID();
        UUID destinationAccountId = UUID.randomUUID();

        Account sourceAccount = new Account();
        sourceAccount.setId(sourceAccountId);
        Account destinationAccount = new Account();
        destinationAccount.setId(destinationAccountId);

        Transaction transaction = Transaction.builder()
                .id(transactionId)
                .tipo(TransactionType.TRANSFERENCIA)
                .valor(new BigDecimal("150.00"))
                .dataHora(LocalDateTime.of(2025, 6, 8, 10, 0, 0))
                .descricao("Transferencia de teste")
                .contaDeOrigem(sourceAccount)
                .contaDeDestino(destinationAccount)
                .build();

        TransactionDTO dto = mapper.toDTO(transaction);
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(transactionId);
        assertThat(dto.getTipo()).isEqualTo(TransactionType.TRANSFERENCIA);
        assertThat(dto.getValor()).isEqualByComparingTo("150.00");
        assertThat(dto.getDataHora()).isEqualTo(LocalDateTime.of(2025, 6, 8, 10, 0, 0));
        assertThat(dto.getDescricao()).isEqualTo("Transferencia de teste");
        assertThat(dto.getContaDeOrigem()).isEqualTo(sourceAccountId);
        assertThat(dto.getContaDeDestino()).isEqualTo(destinationAccountId);
    }

    @Test
    @DisplayName("Should map TransactionDTO to Transaction correctly")
    void shouldMapTransactionDTOToTransaction() {
        UUID transactionId = UUID.randomUUID();
        UUID sourceAccountId = UUID.randomUUID();
        UUID destinationAccountId = UUID.randomUUID();

        TransactionDTO dto = TransactionDTO.builder()
                .id(transactionId)
                .tipo(TransactionType.SAQUE)
                .valor(new BigDecimal("50.00"))
                .dataHora(LocalDateTime.of(2025, 6, 8, 11, 30, 0))
                .descricao("Saque de teste")
                .contaDeOrigem(sourceAccountId)
                .contaDeDestino(null)
                .build();

        Transaction transaction = mapper.toEntity(dto);
        assertThat(transaction).isNotNull();
        assertThat(transaction.getId()).isEqualTo(transactionId);
        assertThat(transaction.getTipo()).isEqualTo(TransactionType.SAQUE);
        assertThat(transaction.getValor()).isEqualByComparingTo("50.00");
        assertThat(transaction.getDataHora()).isEqualTo(LocalDateTime.of(2025, 6, 8, 11, 30, 0));
        assertThat(transaction.getDescricao()).isEqualTo("Saque de teste");
        assertThat(transaction.getContaDeOrigem()).isNotNull();
        assertThat(transaction.getContaDeOrigem().getId()).isEqualTo(sourceAccountId);
        assertThat(transaction.getContaDeDestino()).isNull();
    }

    @Test
    @DisplayName("Should handle null Account IDs when mapping to Entity")
    void shouldHandleNullAccountIdsWhenMappingToEntity() {
        TransactionDTO dto = TransactionDTO.builder()
                .id(UUID.randomUUID())
                .tipo(TransactionType.DEPOSITO)
                .valor(new BigDecimal("200.00"))
                .dataHora(LocalDateTime.now())
                .descricao("Deposito sem conta de origem")
                .contaDeOrigem(null) // Nulo
                .contaDeDestino(UUID.randomUUID())
                .build();

        Transaction transaction = mapper.toEntity(dto);
        assertThat(transaction.getContaDeOrigem()).isNull();
        assertThat(transaction.getContaDeDestino()).isNotNull();
    }

    @Test
    @DisplayName("Should handle null Account objects when mapping to DTO")
    void shouldHandleNullAccountObjectsWhenMappingToDTO() {
        Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID())
                .tipo(TransactionType.DEPOSITO)
                .valor(new BigDecimal("200.00"))
                .dataHora(LocalDateTime.now())
                .descricao("Deposito sem conta de origem")
                .contaDeOrigem(null) 
                .contaDeDestino(new Account() {{ setId(UUID.randomUUID()); }})
                .build();

        
        TransactionDTO dto = mapper.toDTO(transaction);
        assertThat(dto.getContaDeOrigem()).isNull();
        assertThat(dto.getContaDeDestino()).isNotNull();
    }
}
