package com.bradesco.antifraud.controller;

import com.bradesco.antifraud.dto.TransactionDTO;
import com.bradesco.antifraud.mapper.TransactionMapper;
import com.bradesco.antifraud.model.Account;
import com.bradesco.antifraud.model.Transaction;
import com.bradesco.antifraud.model.Transaction.TransactionType;
import com.bradesco.antifraud.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException; 

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList; 
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq; 
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
@DisplayName("TransactionController Tests")
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private TransactionMapper transactionMapper;

    private UUID transactionId;
    private UUID sourceAccountId;
    private UUID destinationAccountId;
    private Transaction sampleTransaction;
    private TransactionDTO sampleTransactionDTO;

    @BeforeEach
    void setUp() {
        transactionId = UUID.randomUUID();
        sourceAccountId = UUID.randomUUID();
        destinationAccountId = UUID.randomUUID();

        Account mockSourceAccount = new Account();
        mockSourceAccount.setId(sourceAccountId);
        Account mockDestinationAccount = new Account();
        mockDestinationAccount.setId(destinationAccountId);

        sampleTransaction = Transaction.builder()
                .id(transactionId)
                .tipo(TransactionType.TRANSFERENCIA)
                .valor(new BigDecimal("100.00"))
                .dataHora(LocalDateTime.now())
                .descricao("Transferencia de teste")
                .contaDeOrigem(mockSourceAccount)
                .contaDeDestino(mockDestinationAccount)
                .build();

        sampleTransactionDTO = TransactionDTO.builder()
                .id(transactionId)
                .tipo(TransactionType.TRANSFERENCIA)
                .valor(new BigDecimal("100.00"))
                .dataHora(LocalDateTime.now())
                .descricao("Transferencia de teste")
                .contaDeOrigem(sourceAccountId)
                .contaDeDestino(destinationAccountId)
                .build();
    }

    @Test
    @DisplayName("POST /transactions - Should create a DEPOSITO transaction successfully and return 201 Created")
    void shouldCreateDepositoTransactionSuccessfully() throws Exception {
        TransactionDTO depositoRequestDTO = TransactionDTO.builder()
                .tipo(TransactionType.DEPOSITO)
                .valor(new BigDecimal("200.00"))
                .dataHora(LocalDateTime.now())
                .descricao("Deposito de teste")
                .contaDeDestino(destinationAccountId)
                .build();

        Transaction depositoEntity = Transaction.builder()
                .tipo(TransactionType.DEPOSITO)
                .valor(new BigDecimal("200.00"))
                .dataHora(depositoRequestDTO.getDataHora()) 
                .descricao("Deposito de teste")
                .contaDeDestino(new Account() {{ setId(destinationAccountId); }})
                .build();
        depositoEntity.setId(UUID.randomUUID()); 

        TransactionDTO depositoResponseDTO = TransactionDTO.builder()
                .id(depositoEntity.getId())
                .tipo(TransactionType.DEPOSITO)
                .valor(new BigDecimal("200.00"))
                .dataHora(depositoRequestDTO.getDataHora()) 
                .descricao("Deposito de teste")
                .contaDeDestino(destinationAccountId)
                .build();

        when(transactionMapper.toEntity(any(TransactionDTO.class))).thenReturn(depositoEntity);
        when(transactionService.create(any(Transaction.class))).thenReturn(depositoEntity);
        when(transactionMapper.toDTO(any(Transaction.class))).thenReturn(depositoResponseDTO);

        mockMvc.perform(post("/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(depositoRequestDTO))) 
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.tipo").value(TransactionType.DEPOSITO.name()))
                .andExpect(jsonPath("$.valor").value(200.00));

        verify(transactionMapper, times(1)).toEntity(any(TransactionDTO.class));
        verify(transactionService, times(1)).create(any(Transaction.class));
        verify(transactionMapper, times(1)).toDTO(any(Transaction.class));
    }

    @Test
    @DisplayName("POST /transactions - Should return 400 Bad Request for invalid data (e.g., null value)")
    void shouldReturnBadRequestForInvalidData() throws Exception {
        TransactionDTO invalidDTO = TransactionDTO.builder()
                .tipo(TransactionType.DEPOSITO)
                .valor(null) 
                .dataHora(LocalDateTime.now())
                .descricao("Invalid transaction")
                .contaDeDestino(destinationAccountId)
                .build();

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed for object='transactionDTO'. Error count: 1"))
                .andExpect(jsonPath("$.errors[0].field").value("valor"))
                .andExpect(jsonPath("$.errors[0].defaultMessage").value("O valor da transação é obrigatório."));

        verify(transactionMapper, never()).toEntity(any(TransactionDTO.class));
        verify(transactionService, never()).create(any(Transaction.class));
    }

    @Test
    @DisplayName("POST /transactions - Should return 409 Conflict for rule violation (DEPOSITO with source account)")
    void shouldReturnConflictForRuleViolationDeposito() throws Exception {
        TransactionDTO invalidDepositoDTO = TransactionDTO.builder()
                .tipo(TransactionType.DEPOSITO)
                .valor(new BigDecimal("100.00"))
                .dataHora(LocalDateTime.now())
                .descricao("Depósito com conta de origem (erro)")
                .contaDeOrigem(sourceAccountId) // Viola a regra do serviço
                .contaDeDestino(destinationAccountId)
                .build();

        Transaction invalidDepositoEntity = Transaction.builder()
                .tipo(TransactionType.DEPOSITO)
                .valor(new BigDecimal("100.00"))
                .dataHora(invalidDepositoDTO.getDataHora())
                .descricao("Depósito com conta de origem (erro)")
                .contaDeOrigem(new Account() {{ setId(sourceAccountId); }})
                .contaDeDestino(new Account() {{ setId(destinationAccountId); }})
                .build();

        when(transactionMapper.toEntity(any(TransactionDTO.class))).thenReturn(invalidDepositoEntity);
        
        when(transactionService.create(any(Transaction.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Deposit should not have a source account."));

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDepositoDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Deposit should not have a source account."));

        verify(transactionMapper, times(1)).toEntity(any(TransactionDTO.class));
        verify(transactionService, times(1)).create(any(Transaction.class));
        verify(transactionMapper, never()).toDTO(any(Transaction.class)); // O mapper toDTO não deve ser chamado em caso de erro
    }


    @Test
    @DisplayName("GET /transactions/{id} - Should return transaction when found")
    void shouldReturnTransactionWhenFound() throws Exception {
        when(transactionService.findById(transactionId)).thenReturn(Optional.of(sampleTransaction));
        when(transactionMapper.toDTO(any(Transaction.class))).thenReturn(sampleTransactionDTO);

        mockMvc.perform(get("/transactions/{id}", transactionId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transactionId.toString()))
                .andExpect(jsonPath("$.tipo").value(TransactionType.TRANSFERENCIA.name()))
                .andExpect(jsonPath("$.valor").value(100.00));

        verify(transactionService, times(1)).findById(transactionId);
        verify(transactionMapper, times(1)).toDTO(any(Transaction.class));
    }

    @Test
    @DisplayName("GET /transactions/{id} - Should return 404 Not Found when transaction not found")
    void shouldReturnNotFoundWhenTransactionNotFound() throws Exception {
        when(transactionService.findById(any(UUID.class))).thenReturn(Optional.empty());

        mockMvc.perform(get("/transactions/{id}", UUID.randomUUID())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(transactionService, times(1)).findById(any(UUID.class)); 
        verify(transactionMapper, never()).toDTO(any(Transaction.class));
    }

    @Test
    @DisplayName("PUT /transactions/{id} - Should update transaction successfully and return 200 OK")
    void shouldUpdateTransactionSuccessfully() throws Exception {
     
        TransactionDTO updatedRequestDTO = TransactionDTO.builder()
                .id(transactionId) 
                .tipo(TransactionType.DEPOSITO)
                .valor(new BigDecimal("175.50"))
                .dataHora(LocalDateTime.now().plusHours(1))
                .descricao("Deposito atualizado")
                .contaDeOrigem(null)
                .contaDeDestino(destinationAccountId)
                .build();

        Transaction updatedEntity = Transaction.builder()
                .id(transactionId)
                .tipo(TransactionType.DEPOSITO)
                .valor(new BigDecimal("175.50"))
                .dataHora(updatedRequestDTO.getDataHora())
                .descricao("Deposito atualizado")
                .contaDeOrigem(null)
                .contaDeDestino(new Account() {{ setId(destinationAccountId); }})
                .build();

        TransactionDTO updatedResponseDTO = TransactionDTO.builder()
                .id(transactionId)
                .tipo(TransactionType.DEPOSITO)
                .valor(new BigDecimal("175.50"))
                .dataHora(updatedRequestDTO.getDataHora())
                .descricao("Deposito atualizado")
                .contaDeOrigem(null)
                .contaDeDestino(destinationAccountId)
                .build();

        when(transactionMapper.toEntity(any(TransactionDTO.class))).thenReturn(updatedEntity);
        when(transactionService.update(eq(transactionId), any(Transaction.class))).thenReturn(updatedEntity);
        when(transactionMapper.toDTO(any(Transaction.class))).thenReturn(updatedResponseDTO);

        mockMvc.perform(put("/transactions/{id}", transactionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transactionId.toString()))
                .andExpect(jsonPath("$.valor").value(175.50))
                .andExpect(jsonPath("$.tipo").value(TransactionType.DEPOSITO.name()));

        verify(transactionMapper, times(1)).toEntity(any(TransactionDTO.class));
        verify(transactionService, times(1)).update(eq(transactionId), any(Transaction.class));
        verify(transactionMapper, times(1)).toDTO(any(Transaction.class));
    }

    @Test
    @DisplayName("PUT /transactions/{id} - Should return 404 Not Found when updating non-existent transaction")
    void shouldReturnNotFoundWhenUpdatingNonExistentTransaction() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        TransactionDTO updatedDTO = TransactionDTO.builder()
                .id(nonExistentId)
                .tipo(TransactionType.TRANSFERENCIA)
                .valor(new BigDecimal("50.00"))
                .dataHora(LocalDateTime.now())
                .descricao("Tentativa de atualização")
                .contaDeOrigem(sourceAccountId)
                .contaDeDestino(destinationAccountId)
                .build();

        Transaction updatedEntity = Transaction.builder()
                .id(nonExistentId)
                .tipo(TransactionType.TRANSFERENCIA)
                .valor(new BigDecimal("50.00"))
                .dataHora(updatedDTO.getDataHora())
                .descricao("Tentativa de atualização")
                .contaDeOrigem(new Account() {{ setId(sourceAccountId); }})
                .contaDeDestino(new Account() {{ setId(destinationAccountId); }})
                .build();

        when(transactionMapper.toEntity(any(TransactionDTO.class))).thenReturn(updatedEntity);
        when(transactionService.update(eq(nonExistentId), any(Transaction.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Transaction not found"));

        mockMvc.perform(put("/transactions/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Transaction not found"));

        verify(transactionMapper, times(1)).toEntity(any(TransactionDTO.class));
        verify(transactionService, times(1)).update(eq(nonExistentId), any(Transaction.class));
        verify(transactionMapper, never()).toDTO(any(Transaction.class));
    }

    @Test
    @DisplayName("DELETE /transactions/{id} - Should delete transaction successfully and return 204 No Content")
    void shouldDeleteTransactionSuccessfully() throws Exception {
        doNothing().when(transactionService).delete(transactionId);
        mockMvc.perform(delete("/transactions/{id}", transactionId))
                .andExpect(status().isNoContent());

        verify(transactionService, times(1)).delete(transactionId);
    }

    @Test
    @DisplayName("DELETE /transactions/{id} - Should return 404 Not Found when deleting non-existent transaction")
    void shouldReturnNotFoundWhenDeletingNonExistentTransaction() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Requested transaction not found"))
                .when(transactionService).delete(nonExistentId);
        mockMvc.perform(delete("/transactions/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Requested transaction not found"));

        verify(transactionService, times(1)).delete(nonExistentId);
    }

    @Test
    @DisplayName("GET /transactions - Should return all transactions")
    void shouldReturnAllTransactions() throws Exception {
      
        Transaction trans1 = Transaction.builder().id(UUID.randomUUID()).tipo(TransactionType.DEPOSITO).valor(new BigDecimal("100")).dataHora(LocalDateTime.now()).contaDeDestino(new Account() {{ setId(destinationAccountId); }}).build();
        Transaction trans2 = Transaction.builder().id(UUID.randomUUID()).tipo(TransactionType.SAQUE).valor(new BigDecimal("50")).dataHora(LocalDateTime.now()).contaDeOrigem(new Account() {{ setId(sourceAccountId); }}).build();

        TransactionDTO dto1 = TransactionDTO.builder().id(trans1.getId()).tipo(TransactionType.DEPOSITO).valor(new BigDecimal("100")).dataHora(trans1.getDataHora()).contaDeDestino(destinationAccountId).build();
        TransactionDTO dto2 = TransactionDTO.builder().id(trans2.getId()).tipo(TransactionType.SAQUE).valor(new BigDecimal("50")).dataHora(trans2.getDataHora()).contaDeOrigem(sourceAccountId).build();

        List<Transaction> transactions = new ArrayList<>(Arrays.asList(trans1, trans2));
        List<TransactionDTO> transactionDTOs = new ArrayList<>(Arrays.asList(dto1, dto2)); 

        when(transactionService.findAll()).thenReturn(transactions); 
        when(transactionMapper.toDTO(trans1)).thenReturn(dto1);
        when(transactionMapper.toDTO(trans2)).thenReturn(dto2);
        when(transactionMapper.toDTO(any(List.class))).thenReturn(transactionDTOs);

        mockMvc.perform(get("/transactions")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(dto1.getId().toString()))
                .andExpect(jsonPath("$[1].id").value(dto2.getId().toString()));

        verify(transactionService, times(1)).findAll(); 
        verify(transactionMapper, times(2)).toDTO(any(Transaction.class)); 
    }
}
