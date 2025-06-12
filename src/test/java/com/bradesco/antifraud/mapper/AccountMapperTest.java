package com.bradesco.antifraud.mapper;

import com.bradesco.antifraud.dto.AccountDto;
import com.bradesco.antifraud.model.Account;
import com.bradesco.antifraud.model.Account.AccountStatus;
import com.bradesco.antifraud.model.Account.AccountType;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AccountMapperTest {

    private final AccountMapper accountMapper = Mappers.getMapper(AccountMapper.class);

    @Test
    void testToDTO() {
        Account account = new Account();
        account.setId(UUID.randomUUID());
        account.setAccountNumber("12345");
        account.setAgency("001");
        account.setBalance(BigDecimal.TEN);
        account.setAccountType(AccountType.CORRENTE);
        account.setAccountStatus(AccountStatus.ATIVA);

        AccountDto accountDTO = accountMapper.toDto(account);

        assertEquals(account.getId(), accountDTO.getId());
        assertEquals(account.getAccountNumber(), accountDTO.getAccountNumber());
        assertEquals(account.getAgency(), accountDTO.getAgency());
        assertEquals(account.getBalance(), accountDTO.getBalance());
        assertEquals(account.getAccountType(), accountDTO.getAccountType());
        assertEquals(account.getAccountStatus(), accountDTO.getAccountStatus());
    }

    @Test
    void testToEntity() {
        AccountDto accountDTO = AccountDto.builder()
                .id(UUID.randomUUID())
                .accountNumber("12345")
                .agency("001")
                .balance(BigDecimal.TEN)
                .accountType(AccountType.POUPANCA)
                .accountStatus(AccountStatus.INATIVA)
                .build();

        Account account = accountMapper.toEntity(accountDTO);

        assertEquals(accountDTO.getId(), account.getId());
        assertEquals(accountDTO.getAccountNumber(), account.getAccountNumber());
        assertEquals(accountDTO.getAgency(), account.getAgency());
        assertEquals(accountDTO.getBalance(), account.getBalance());
        assertEquals(accountDTO.getAccountType(), account.getAccountType());
        assertEquals(accountDTO.getAccountStatus(), account.getAccountStatus());
    }
}