package com.bradesco.antifraud.service;

import com.bradesco.antifraud.model.Account;
import com.bradesco.antifraud.model.Transaction;
import com.bradesco.antifraud.model.Transaction.TransactionType;
import com.bradesco.antifraud.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService Tests")
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    private UUID transactionId;
    private UUID sourceAccountId;
    private UUID destinationAccountId;
    private Account sourceAccount;
    private Account destinationAccount;
    private Transaction sampleTransaction;

    @BeforeEach
    void setUp() {
        transactionId = UUID.randomUUID();
        sourceAccountId = UUID.randomUUID();
        destinationAccountId = UUID.randomUUID();

        sourceAccount = new Account();
        sourceAccount.setId(sourceAccountId);
        sourceAccount.setNumeroConta("11111");
        sourceAccount.setAgencia("0001");
        sourceAccount.setSaldo(new BigDecimal("1000.00"));
        sourceAccount.setTipoConta(Account.AccountType.CORRENTE);
        sourceAccount.setStatus(Account.AccountStatus.ATIVA);
        sourceAccount.setCustomerId(UUID.randomUUID());

        destinationAccount = new Account();
        destinationAccount.setId(destinationAccountId);
        destinationAccount.setNumeroConta("22222");
        destinationAccount.setAgencia("0001");
        destinationAccount.setSaldo(new BigDecimal("500.00"));
        destinationAccount.setTipoConta(Account.AccountType.POUPANCA);
        destinationAccount.setStatus(Account.AccountStatus.ATIVA);
        destinationAccount.setCustomerId(UUID.randomUUID());

        sampleTransaction = Transaction.builder()
                .id(transactionId)
                .tipo(TransactionType.TRANSFERENCIA)
                .valor(new BigDecimal("100.00"))
                .dataHora(LocalDateTime.now())
                .descricao("Transferencia de teste")
                .contaDeOrigem(sourceAccount)
                .contaDeDestino(destinationAccount)
                .build();
    }

    @Test
    @DisplayName("Should create a DEPOSITO transaction successfully")
    void shouldCreateDepositoTransactionSuccessfully() {
        Transaction depositoTransaction = Transaction.builder()
                .tipo(TransactionType.DEPOSITO)
                .valor(new BigDecimal("200.00"))
                .dataHora(LocalDateTime.now())
                .descricao("Deposito de teste")
                .contaDeDestino(destinationAccount)
                .build();

        when(transactionRepository.save(any(Transaction.class))).thenReturn(depositoTransaction);

        Transaction createdTransaction = transactionService.create(depositoTransaction);

        assertThat(createdTransaction).isNotNull();
        assertThat(createdTransaction.getTipo()).isEqualTo(TransactionType.DEPOSITO);
        assertThat(createdTransaction.getContaDeDestino()).isEqualTo(destinationAccount);
        verify(transactionRepository, times(1)).save(depositoTransaction);
    }

    @Test
    @DisplayName("Should create a SAQUE transaction successfully")
    void shouldCreateSaqueTransactionSuccessfully() {
        Transaction saqueTransaction = Transaction.builder()
                .tipo(TransactionType.SAQUE)
                .valor(new BigDecimal("50.00"))
                .dataHora(LocalDateTime.now())
                .descricao("Saque de teste")
                .contaDeOrigem(sourceAccount)
                .build();

        when(transactionRepository.save(any(Transaction.class))).thenReturn(saqueTransaction);

        Transaction createdTransaction = transactionService.create(saqueTransaction);

        assertThat(createdTransaction).isNotNull();
        assertThat(createdTransaction.getTipo()).isEqualTo(TransactionType.SAQUE);
        assertThat(createdTransaction.getContaDeOrigem()).isEqualTo(sourceAccount);
        verify(transactionRepository, times(1)).save(saqueTransaction);
    }

    @Test
    @DisplayName("Should create a TRANSFERENCIA transaction successfully")
    void shouldCreateTransferenciaTransactionSuccessfully() {
        
        Transaction transferenciaTransaction = sampleTransaction; // Already configured
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transferenciaTransaction);
       
        Transaction createdTransaction = transactionService.create(transferenciaTransaction);

        assertThat(createdTransaction).isNotNull();
        assertThat(createdTransaction.getTipo()).isEqualTo(TransactionType.TRANSFERENCIA);
        assertThat(createdTransaction.getContaDeOrigem()).isEqualTo(sourceAccount);
        assertThat(createdTransaction.getContaDeDestino()).isEqualTo(destinationAccount);
        verify(transactionRepository, times(1)).save(transferenciaTransaction);
    }

    @Test
    @DisplayName("Should create a PAGAMENTO transaction successfully")
    void shouldCreatePagamentoTransactionSuccessfully() {
        Transaction pagamentoTransaction = Transaction.builder()
                .tipo(TransactionType.PAGAMENTO)
                .valor(new BigDecimal("120.50"))
                .dataHora(LocalDateTime.now())
                .descricao("Pagamento de conta de luz")
                .contaDeOrigem(sourceAccount)
                .build();

        when(transactionRepository.save(any(Transaction.class))).thenReturn(pagamentoTransaction);
        Transaction createdTransaction = transactionService.create(pagamentoTransaction);

        assertThat(createdTransaction).isNotNull();
        assertThat(createdTransaction.getTipo()).isEqualTo(TransactionType.PAGAMENTO);
        assertThat(createdTransaction.getContaDeOrigem()).isEqualTo(sourceAccount);
        verify(transactionRepository, times(1)).save(pagamentoTransaction);
    }

    @Test
    @DisplayName("Should throw CONFLICT for DEPOSITO with source account")
    void shouldThrowConflictForDepositoWithSourceAccount() {
        Transaction invalidDeposito = Transaction.builder()
                .tipo(TransactionType.DEPOSITO)
                .valor(new BigDecimal("100.00"))
                .dataHora(LocalDateTime.now())
                .descricao("Depósito inválido")
                .contaDeOrigem(sourceAccount) 
                .contaDeDestino(destinationAccount)
                .build();

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> transactionService.create(invalidDeposito));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getReason()).isEqualTo("Para depósito, não deve haver conta de origem.");
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should throw CONFLICT for SAQUE without source account")
    void shouldThrowConflictForSaqueWithoutSourceAccount() {
        Transaction invalidSaque = Transaction.builder()
                .tipo(TransactionType.SAQUE)
                .valor(new BigDecimal("50.00"))
                .dataHora(LocalDateTime.now())
                .descricao("Saque inválido")
                .contaDeOrigem(null) 
                .contaDeDestino(null)
                .build();

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> transactionService.create(invalidSaque));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getReason()).isEqualTo("Para saque, deve haver uma conta de origem.");
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should throw CONFLICT for TRANSFERENCIA with same source and destination accounts")
    void shouldThrowConflictForTransferenciaWithSameAccounts() {
        Transaction invalidTransferencia = Transaction.builder()
                .tipo(TransactionType.TRANSFERENCIA)
                .valor(new BigDecimal("10.00"))
                .dataHora(LocalDateTime.now())
                .descricao("Transferência para a mesma conta")
                .contaDeOrigem(sourceAccount)
                .contaDeDestino(sourceAccount)
                .build();

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> transactionService.create(invalidTransferencia));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getReason()).isEqualTo("Conta de origem e destino não podem ser a mesma para transferência.");
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should find transaction by ID successfully")
    void shouldFindTransactionByIdSuccessfully() {
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(sampleTransaction));

        Optional<Transaction> foundTransaction = transactionService.getAccountById(transactionId); 

        assertThat(foundTransaction).isPresent();
        assertThat(foundTransaction.get().getId()).isEqualTo(transactionId);
        verify(transactionRepository, times(1)).findById(transactionId);
    }

    @Test
    @DisplayName("Should return empty when transaction not found by ID")
    void shouldReturnEmptyWhenTransactionNotFoundById() {
        UUID nonExistentId = UUID.randomUUID();
        when(transactionRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        Optional<Transaction> foundTransaction = transactionService.getAccountById(nonExistentId);

        assertThat(foundTransaction).isEmpty();
        verify(transactionRepository, times(1)).findById(nonExistentId);
    }

    @Test
    @DisplayName("Should update transaction successfully")
    void shouldUpdateTransactionSuccessfully() {
        Transaction updatedDetails = Transaction.builder()
                .id(transactionId) 
                .tipo(TransactionType.DEPOSITO) 
                .valor(new BigDecimal("250.00"))
                .dataHora(LocalDateTime.now().plusHours(1))
                .descricao("Depósito atualizado")
                .contaDeOrigem(null)
                .contaDeDestino(destinationAccount)
                .build();

        when(transactionRepository.existsById(transactionId)).thenReturn(true);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(updatedDetails);
        Transaction result = transactionService.update(transactionId, updatedDetails);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(transactionId);
        assertThat(result.getValor()).isEqualByComparingTo("250.00");
        assertThat(result.getTipo()).isEqualTo(TransactionType.DEPOSITO);
        verify(transactionRepository, times(1)).existsById(transactionId);
        verify(transactionRepository, times(1)).save(updatedDetails);
    }

    @Test
    @DisplayName("Should throw NOT_FOUND when updating non-existent transaction")
    void shouldThrowNotFoundWhenUpdatingNonExistentTransaction() {
        UUID nonExistentId = UUID.randomUUID();
        when(transactionRepository.existsById(nonExistentId)).thenReturn(false);

        Transaction updatedDetails = Transaction.builder()
                .id(nonExistentId)
                .tipo(TransactionType.TRANSFERENCIA)
                .valor(new BigDecimal("50.00"))
                .dataHora(LocalDateTime.now())
                .descricao("Tentativa de atualização")
                .contaDeOrigem(sourceAccount)
                .contaDeDestino(destinationAccount)
                .build();

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> transactionService.update(nonExistentId, updatedDetails));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).isEqualTo("Transação não encontrada.");
        verify(transactionRepository, times(1)).existsById(nonExistentId);
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should throw CONFLICT when updating with invalid rules (e.g., SAQUE with destination)")
    void shouldThrowConflictWhenUpdatingWithInvalidRules() {
        Transaction invalidUpdate = Transaction.builder()
                .id(transactionId)
                .tipo(TransactionType.SAQUE)
                .valor(new BigDecimal("60.00"))
                .dataHora(LocalDateTime.now())
                .descricao("Saque com destino na atualização (erro)")
                .contaDeOrigem(sourceAccount)
                .contaDeDestino(destinationAccount) 
                .build();

        when(transactionRepository.existsById(transactionId)).thenReturn(true);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> transactionService.update(transactionId, invalidUpdate));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exception.getReason()).isEqualTo("Para saque, não deve haver conta de destino.");
        verify(transactionRepository, times(1)).existsById(transactionId);
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    @DisplayName("Should delete transaction successfully")
    void shouldDeleteTransactionSuccessfully() {
        when(transactionRepository.existsById(transactionId)).thenReturn(true);
        doNothing().when(transactionRepository).deleteById(transactionId);
        transactionService.delete(transactionId);
        verify(transactionRepository, times(1)).existsById(transactionId);
        verify(transactionRepository, times(1)).deleteById(transactionId);
    }

    @Test
    @DisplayName("Should throw NOT_FOUND when deleting non-existent transaction")
    void shouldThrowNotFoundWhenDeletingNonExistentTransaction() {
        UUID nonExistentId = UUID.randomUUID();
        when(transactionRepository.existsById(nonExistentId)).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> transactionService.delete(nonExistentId));

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(exception.getReason()).isEqualTo("Transação não encontrada.");
        verify(transactionRepository, times(1)).existsById(nonExistentId);
        verify(transactionRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    @DisplayName("Should get all transactions successfully")
    void shouldGetAllTransactionsSuccessfully() {
        Transaction trans1 = Transaction.builder().id(UUID.randomUUID()).tipo(TransactionType.DEPOSITO).valor(new BigDecimal("100")).dataHora(LocalDateTime.now()).contaDeDestino(destinationAccount).build();
        Transaction trans2 = Transaction.builder().id(UUID.randomUUID()).tipo(TransactionType.SAQUE).valor(new BigDecimal("50")).dataHora(LocalDateTime.now()).contaDeOrigem(sourceAccount).build();
        List<Transaction> transactions = Arrays.asList(trans1, trans2);

        when(transactionRepository.findAll()).thenReturn(transactions);

        List<Transaction> result = transactionService.getAllTransactions();

        assertThat(result).isNotNull().hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(trans1, trans2);
        verify(transactionRepository, times(1)).findAll();
    }
}