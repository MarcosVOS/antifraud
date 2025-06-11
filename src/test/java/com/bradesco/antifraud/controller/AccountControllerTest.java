package com.bradesco.antifraud.controller;

import com.bradesco.antifraud.dto.AccountDto;
import com.bradesco.antifraud.mapper.AccountMapper;

import com.bradesco.antifraud.model.Account;
import com.bradesco.antifraud.model.Account.AccountStatus;
import com.bradesco.antifraud.model.Account.AccountType;

import com.bradesco.antifraud.model.Address;
import com.bradesco.antifraud.model.Customer;
import com.bradesco.antifraud.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

@Slf4j
@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
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

    private Address mockAddress;
    private Customer mockCustomer;


    @BeforeEach
    void setup() {
        mockAddress = Address.builder()
                .street("Main St")
                .number("123")
                .neighborhood("Downtown")
                .city("Anytown")
                .state("YY")
                .zipCode("12345-678")
                .build();

        mockCustomer = Customer.builder()
                .id(customerId)
                .name("John Doe")
                .cpf("12345678909")
                .dateOfBirth(LocalDate.of( 1990,  1, 1))
                .email("john.doe@example.com")
                .phone("+551199998888")
                .address(mockAddress)
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
                .customer(mockCustomer) // Use the mock customer created in setup
                .build();
        AccountDto dto = AccountDto.builder()
                .id(id)
                .accountNumber("12345")
                .agency("001")
                .balance(BigDecimal.TEN)
                .accountType(Account.AccountType.CORRENTE)
                .accountStatus(Account.AccountStatus.ATIVA)
                .customerId(customerId)
                .build();

        Mockito.when(accountService.getAccountById(id)).thenReturn(Optional.of(account));
        Mockito.when(accountMapper.toDto(account)).thenReturn(dto);


        mockMvc.perform(get("/accounts/"+ id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.account_number").value("12345")) // Corrigido
                .andExpect(jsonPath("$.agency").value("001"))
                .andExpect(jsonPath("$.balance").value(BigDecimal.TEN.doubleValue()))
                .andExpect(jsonPath("$.account_type").value(AccountType.CORRENTE.toString())) // Corrigido
                .andExpect(jsonPath("$.account_status").value(AccountStatus.ATIVA.toString()));
    }

    @Test
    void getAccountByID_notFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(accountService.getAccountById(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/accounts/"))
                .andExpect(status().isNotFound());
    }


}