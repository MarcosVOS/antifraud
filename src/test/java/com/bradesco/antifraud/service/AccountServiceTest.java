package com.bradesco.antifraud.service;

import com.bradesco.antifraud.model.Account;
import com.bradesco.antifraud.model.Customer; // Supondo que Customer exista
import com.bradesco.antifraud.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    private Account account;
    private UUID accountId;
    private Customer customer; // Supondo que Customer exista
    private UUID customerId; // Supondo que Customer exista

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
        customerId = UUID.randomUUID(); // Supondo que Customer exista
        customer = Customer.builder().id(customerId).build(); // Supondo que Customer exista e tenha builder

        account = Account.builder()
                .id(accountId)
                .accountNumber("12345")
                .agency("001")
                .balance(BigDecimal.TEN)
                .accountType(Account.AccountType.CORRENTE)
                .accountStatus(Account.AccountStatus.ATIVA)
                .customerId(customer) // Supondo que Account tenha um campo Customer
                .build();
    }

    @Test
    void getAccountById_whenAccountExists_shouldReturnAccount() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        Optional<Account> foundAccount = accountService.getAccountById(accountId);

        assertTrue(foundAccount.isPresent());
        assertEquals(account, foundAccount.get());
        verify(accountRepository).findById(accountId);
    }

    @Test
    void getAccountById_whenAccountDoesNotExist_shouldReturnEmpty() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        Optional<Account> foundAccount = accountService.getAccountById(accountId);

        assertFalse(foundAccount.isPresent());
        verify(accountRepository).findById(accountId);
    }

    @Test
    void createAccount_shouldSaveAndReturnAccount() {
        Account newAccount = Account.builder()
                .accountNumber("67890")
                .agency("002")
                .balance(new BigDecimal("200.00"))
                .accountType(Account.AccountType.POUPANCA)
                .accountStatus(Account.AccountStatus.ATIVA)
                .customerId(customer)
                .build();
        // O ID será gerado pelo banco de dados, então o mock retorna com ID
        Account savedAccount = Account.builder()
                .id(UUID.randomUUID())
                .accountNumber("67890")
                .agency("002")
                .balance(new BigDecimal("200.00"))
                .accountType(Account.AccountType.POUPANCA)
                .accountStatus(Account.AccountStatus.ATIVA)
                .customerId(customer)
                .build();

        when(accountRepository.save(newAccount)).thenReturn(savedAccount);

        Account result = accountService.createAccount(newAccount);

        assertNotNull(result.getId());
        assertEquals(savedAccount, result);
        verify(accountRepository).save(newAccount);
    }

    @Test
    void deleteAccount_shouldCallRepositoryDeleteById() {
        doNothing().when(accountRepository).deleteById(accountId);

        accountService.deleteAccount(accountId);

        verify(accountRepository).deleteById(accountId);
    }

    @Test
    void updateAccount_whenAccountExists_shouldUpdateAndReturnAccount() {
        Account updatedInfo = Account.builder()
                .accountNumber("12345-updated")
                .agency("001-updated")
                .balance(BigDecimal.ONE)
                .accountType(Account.AccountType.POUPANCA)
                .accountStatus(Account.AccountStatus.ATIVA)
                .customerId(customer)
                .build(); // ID não é necessário aqui, será setado pelo serviço

        Account expectedUpdatedAccount = Account.builder()
                .id(accountId) // ID é mantido
                .accountNumber("12345-updated")
                .agency("001-updated")
                .balance(BigDecimal.ONE)
                .accountType(Account.AccountType.POUPANCA)
                .accountStatus(Account.AccountStatus.ATIVA)
                .customerId(customer)
                .build();

        when(accountRepository.existsById(accountId)).thenReturn(true);
        when(accountRepository.save(any(Account.class))).thenReturn(expectedUpdatedAccount);

        Account result = accountService.updateAccount(accountId, updatedInfo);

        assertEquals(expectedUpdatedAccount, result);
        assertEquals(accountId, result.getId()); // Garante que o ID foi setado corretamente
        verify(accountRepository).existsById(accountId);
        // Verifica se o save foi chamado com o objeto Account que tem o ID correto
        verify(accountRepository).save(argThat(savedAccount ->
            savedAccount.getId().equals(accountId) &&
            savedAccount.getAccountNumber().equals("12345-updated")
        ));
    }

    @Test
    void updateAccount_whenAccountDoesNotExist_shouldThrowIllegalArgumentException() {
        UUID nonExistentId = UUID.randomUUID();
        Account updatedInfo = Account.builder().build();

        when(accountRepository.existsById(nonExistentId)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            accountService.updateAccount(nonExistentId, updatedInfo);
        });

        assertEquals("Account with ID " + nonExistentId + " does not exist.", exception.getMessage());
        verify(accountRepository).existsById(nonExistentId);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void accountExists_whenAccountExists_shouldReturnTrue() {
        when(accountRepository.existsById(accountId)).thenReturn(true);

        boolean exists = accountService.accountExists(accountId);

        assertTrue(exists);
        verify(accountRepository).existsById(accountId);
    }

    @Test
    void accountExists_whenAccountDoesNotExist_shouldReturnFalse() {
        when(accountRepository.existsById(accountId)).thenReturn(false);

        boolean exists = accountService.accountExists(accountId);

        assertFalse(exists);
        verify(accountRepository).existsById(accountId);
    }
}
