// src/test/java/com/bradesco/antifraud/controller/AccountControllerTest.java
package com.bradesco.antifraud.controller;

import com.bradesco.antifraud.dto.AccountDTO;
import com.bradesco.antifraud.mapper.AccountMapper;

import com.bradesco.antifraud.model.Account;
import com.bradesco.antifraud.model.Account.AccountStatus;
import com.bradesco.antifraud.model.Account.AccountType;

import com.bradesco.antifraud.model.Address;
import com.bradesco.antifraud.model.Customer;
import com.bradesco.antifraud.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private AccountMapper accountMapper;
    @Autowired
    private ObjectMapper objectMapper;

    UUID generatedId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();
    private Customer createMockCustomer(UUID customerId) {
        Address address = Address.builder()
                .street("Main St")
                .number("123")
                .neighborhood("Downtown")
                .city("Anytown")
                .state("XX")
                .zipCode("12345-678")
                .build();

        return Customer.builder()
                .id(customerId)
                .name("John Doe")
                .cpf("12345678909") // Use a valid CPF generator for real tests if needed
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .email("john.doe@example.com") // Valid email
                .phone("+5511999998888")
                .address(address)
                .password("securePassword123")
                .build();
    }

    @Test
    void getAccountByID_found_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();

        Account account = Account.builder()
                .id(id)
                .accountNumber("12345")
                .agency("001")
                .balance(BigDecimal.TEN)
                .accountType(AccountType.CORRENTE)
                .accountStatus(AccountStatus.ATIVA)
                .customer(createMockCustomer(customerId))
                .build();
        AccountDTO dto = AccountDTO.builder()
                .id(id)
                .accountNumber("12345")
                .agency("001")
                .balance(BigDecimal.TEN)
                .accountType(Account.AccountType.CORRENTE)
                .accountStatus(Account.AccountStatus.ATIVA)
                .customerId(customerId)
                .build();

        Mockito.when(accountService.getAccountById(id)).thenReturn(Optional.of(account));
        Mockito.when(accountMapper.toDTO(account)).thenReturn(dto);


        mockMvc.perform(get("/accounts/"+ id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.accountNumber").value("12345"))
                .andExpect(jsonPath("$.agency").value("001"))
                .andExpect(jsonPath("$.balance").value(BigDecimal.TEN.doubleValue())) // Para BigDecimal, compare o valor double
                .andExpect(jsonPath("$.accountType").value(AccountType.CORRENTE.toString()))
                .andExpect(jsonPath("$.accountStatus").value(AccountStatus.ATIVA.toString()));
    }

    @Test
    void getAccountByID_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(accountService.getAccountById(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/accounts/"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createAccount_returnsCreated() throws Exception {

        // Mockando um ID de cliente, se necessário
        AccountDTO requestDto = AccountDTO.builder()
                .accountNumber("67890")
                .agency("002")
                .balance(new BigDecimal("100.50"))
                .accountType(AccountType.POUPANCA)
                .accountStatus(AccountStatus.ATIVA)
                .customerId(createMockCustomer(customerId).getId())
                .build();

        Account accountEntityToCreate = Account.builder()
                .accountNumber("67890")
                .agency("002")
                .balance(new BigDecimal("100.50"))
                .accountType(AccountType.POUPANCA)
                .accountStatus(AccountStatus.ATIVA)
                .customer(createMockCustomer(customerId))
                .build();

        Account createdAccountEntity = Account.builder()
                .id(generatedId) // O serviço deve gerar o ID
                .accountNumber("67890")
                .agency("002")
                .balance(new BigDecimal("100.50"))
                .accountType(AccountType.POUPANCA)
                .accountStatus(AccountStatus.ATIVA)
                .customer(createMockCustomer(customerId))
                .build();

        AccountDTO responseDto = AccountDTO.builder()
                .id(generatedId)
                .accountNumber("67890")
                .agency("002")
                .balance(new BigDecimal("100.50"))
                .accountType(AccountType.POUPANCA)
                .accountStatus(AccountStatus.ATIVA)
                .customerId(createMockCustomer(generatedId).getId())
                .build();

        Mockito.when(accountMapper.toEntity(any(AccountDTO.class))).thenReturn(accountEntityToCreate);
        Mockito.when(accountService.createAccount(any(Account.class), customerId)).thenReturn(createdAccountEntity);
        Mockito.when(accountMapper.toDTO(any(Account.class))).thenReturn(responseDto);

        mockMvc.perform(post("/accounts/newaccount")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/accounts/newaccount/"+generatedId)))
                .andExpect(jsonPath("$.id").value(generatedId.toString()))
                .andExpect(jsonPath("$.accountNumber").value("67890"))
                .andExpect(jsonPath("$.agency").value("002"))
                .andExpect(jsonPath("$.balance").value(100.50))
                .andExpect(jsonPath("$.accountType").value(AccountType.POUPANCA.toString()))
                .andExpect(jsonPath("$.accountStatus").value(AccountStatus.ATIVA.toString()));
    }
}
