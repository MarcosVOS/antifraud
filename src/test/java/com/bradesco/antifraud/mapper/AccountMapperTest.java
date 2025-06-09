package com.bradesco.antifraud.mapper;

import com.bradesco.antifraud.dto.AccountDTO;
import com.bradesco.antifraud.model.Account;
import com.bradesco.antifraud.model.Account.AccountStatus;
import com.bradesco.antifraud.model.Account.AccountType;
import com.bradesco.antifraud.model.Customer;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import java.math.BigDecimal;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class AccountMapperTest {

    private final AccountMapper accountMapper = Mappers.getMapper(AccountMapper.class);

    @Test
    void testToDTO() {
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer();
        customer.setId(customerId);

        Account account = Account.builder()
                .id(UUID.randomUUID())
                .accountNumber("12345")
                .agency("001")
                .balance(BigDecimal.TEN)
                .accountType(AccountType.CORRENTE)
                .accountStatus(AccountStatus.ATIVA)
                .customer(customer)
                .build();

        AccountDTO accountDTO = accountMapper.toDTO(account);

        assertEquals(account.getId(), accountDTO.getId());
        assertEquals(account.getAccountNumber(), accountDTO.getAccountNumber());
        assertEquals(account.getAgency(), accountDTO.getAgency());
        assertEquals(account.getBalance(), accountDTO.getBalance());
        assertEquals(account.getAccountType(), accountDTO.getAccountType());
        assertEquals(account.getAccountStatus(), accountDTO.getAccountStatus());
        assertEquals(customerId, accountDTO.getCustomerId());
    }

    @Test
    void testToEntity() {
        UUID accountId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        AccountDTO accountDTO = AccountDTO.builder()
                .id(accountId)
                .accountNumber("12345")
                .agency("001")
                .balance(BigDecimal.TEN)
                .accountType(AccountType.POUPANCA)
                .accountStatus(AccountStatus.INATIVA)
                .customerId(customerId)
                .build();

        Account account = accountMapper.toEntity(accountDTO);

        assertEquals(accountDTO.getId(), account.getId());
        assertEquals(accountDTO.getAccountNumber(), account.getAccountNumber());
        assertEquals(accountDTO.getAgency(), account.getAgency());
        assertEquals(accountDTO.getBalance(), account.getBalance());
        assertEquals(accountDTO.getAccountType(), account.getAccountType());
        assertEquals(accountDTO.getAccountStatus(), account.getAccountStatus());
        assertNotNull(account.getCustomer());
        assertEquals(customerId, account.getCustomer().getId());
        assertNull(account.getCustomer().getName()); 
    }
}
