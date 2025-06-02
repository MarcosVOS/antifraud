// src/test/java/com/bradesco/antifraud/controller/AccountControllerTest.java
package com.bradesco.antifraud.controller;

import com.bradesco.antifraud.dto.AccountDTO;
import com.bradesco.antifraud.mapper.AccountMapper;

import com.bradesco.antifraud.model.Account;
import com.bradesco.antifraud.model.Account.AccountStatus;
import com.bradesco.antifraud.model.Account.AccountType;
import com.bradesco.antifraud.service.AccountService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @MockitoBean
    private  AccountMapper accountMapper;

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
                .customerId(null)
                .build();
        AccountDTO dto = AccountDTO.builder()
                .id(id)
                .accountNumber("12345")
                .agency("001")
                .balance(BigDecimal.TEN)
                .accountType(Account.AccountType.CORRENTE)
                .accountStatus(Account.AccountStatus.ATIVA)
                .customerId(null)
                .build();

        Mockito.when(accountService.getAccountById(id)).thenReturn(Optional.of(account));
        Mockito.when(accountMapper.toDTO(account)).thenReturn(dto);


        mockMvc.perform(get("/accounts/" + id))
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

        mockMvc.perform(get("/accounts/" + id))
                .andExpect(status().isNotFound());
    }
}
