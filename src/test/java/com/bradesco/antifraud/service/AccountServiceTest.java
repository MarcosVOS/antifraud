package com.bradesco.antifraud.service;

import com.bradesco.antifraud.dto.AccountDTO;
import com.bradesco.antifraud.exception.accountExceptions.AccountAlreadyExistsException;
import com.bradesco.antifraud.model.Account;
import com.bradesco.antifraud.model.Address;
import com.bradesco.antifraud.model.Customer;
import com.bradesco.antifraud.repository.AccountRepository;
import com.bradesco.antifraud.repository.CustomerRepository;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private AccountService accountService;

    private Account account;
    private UUID accountId;
    private UUID customerId;

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
                .cpf("12345678909")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .email("john.doe@example.com")
                .phone("+5511999998888")
                .address(address)
                .password("securePassword123")
                .build();
    }

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        account = Account.builder()
                .id(accountId)
                .accountNumber("12345")
                .agency("001")
                .balance(BigDecimal.TEN)
                .accountType(Account.AccountType.CORRENTE)
                .accountStatus(Account.AccountStatus.ATIVA)
                .customer(createMockCustomer(customerId))
                .build();
    }

    @Test
    void findById_whenAccountExists_shouldReturnAccount() { // Renomeado o teste
        // Given
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        // When
        Optional<Account> foundAccount = accountService.findById(accountId);

        // Then
        assertTrue(foundAccount.isPresent());
        assertEquals(account, foundAccount.get());
        verify(accountRepository).findById(accountId);
        verifyNoMoreInteractions(accountRepository);
        verifyNoInteractions(customerRepository);
    }

    @Test
    void findById_whenAccountDoesNotExist_shouldReturnEmptyOptional() { // Renomeado o teste
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(accountRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When
        Optional<Account> foundAccount = accountService.findById(nonExistentId);

        // Then
        assertFalse(foundAccount.isPresent());
        verify(accountRepository).findById(nonExistentId);
        verifyNoMoreInteractions(accountRepository);
        verifyNoInteractions(customerRepository);
    }

    @Test
    void createAccount_shouldSaveAndReturnAccount() {
        // Arrange
        String newAccountNumber = "67890";
        UUID generatedAccountId = UUID.randomUUID();

        Account accountDetailsToCreate = Account.builder()
                .accountNumber(newAccountNumber)
                .agency("002")
                .balance(new BigDecimal("200.00"))
                .accountType(Account.AccountType.POUPANCA)
                .accountStatus(Account.AccountStatus.ATIVA)
                .customer(createMockCustomer(customerId))
                .build();

        Account expectedSavedAccount = Account.builder()
                .id(generatedAccountId)
                .accountNumber(newAccountNumber)
                .agency("002")
                .balance(new BigDecimal("200.00"))
                .accountType(Account.AccountType.POUPANCA)
                .accountStatus(Account.AccountStatus.ATIVA)
                .customer(createMockCustomer(customerId))
                .build();

        when(accountRepository.existsByAccountNumber(newAccountNumber)).thenReturn(false);
        when(accountRepository.save(argThat(accToSave ->
                accToSave.getAccountNumber().equals(newAccountNumber) &&
                accToSave.getCustomer().equals(createMockCustomer(customerId)) &&
                accToSave.getId() == null
        ))).thenReturn(expectedSavedAccount);

        // Act
        Account actualSavedAccount = accountService.createAccount(accountDetailsToCreate);

        // Assert
        assertNotNull(actualSavedAccount, "A conta salva não deve ser nula.");
        assertEquals(expectedSavedAccount.getId(), actualSavedAccount.getId(), "O ID da conta salva está incorreto.");
        assertEquals(expectedSavedAccount.getAccountNumber(), actualSavedAccount.getAccountNumber(), "O número da conta está incorreto.");
        assertEquals(expectedSavedAccount.getAgency(), actualSavedAccount.getAgency(), "A agência está incorreta.");
        assertEquals(0, expectedSavedAccount.getBalance().compareTo(actualSavedAccount.getBalance()), "O saldo está incorreto.");
        assertEquals(expectedSavedAccount.getAccountType(), actualSavedAccount.getAccountType(), "O tipo de conta está incorreto.");
        assertEquals(expectedSavedAccount.getAccountStatus(), actualSavedAccount.getAccountStatus(), "O status da conta está incorreto.");
        assertEquals(expectedSavedAccount.getCustomer(), actualSavedAccount.getCustomer(), "O cliente associado está incorreto.");

        // Verify
        verify(accountRepository).existsByAccountNumber(newAccountNumber);
        verify(accountRepository).save(argThat(accToSave ->
                accToSave.getAccountNumber().equals(newAccountNumber) &&
                accToSave.getCustomer().equals(createMockCustomer(customerId)) &&
                accToSave.getId() == null
        ));
        verifyNoMoreInteractions(accountRepository);
        verifyNoInteractions(customerRepository);
    }

    @Test
    void createAccount_whenAccountNumberAlreadyExists_ThrowAccountAlreadyExistsException() {
        // Arrange
        String existingAccountNumber = "12345";

        Account accountAttemptingToCreate = Account.builder()
                .accountNumber(existingAccountNumber)
                .agency("003")
                .balance(new BigDecimal("50.00"))
                .accountType(Account.AccountType.CORRENTE)
                .accountStatus(Account.AccountStatus.ATIVA)
                .customer(createMockCustomer(customerId))
                .build();

        when(accountRepository.existsByAccountNumber(existingAccountNumber)).thenReturn(true);

        // Act & Assert
        AccountAlreadyExistsException exception = assertThrows(AccountAlreadyExistsException.class, () -> {
            accountService.createAccount(accountAttemptingToCreate);
        });

        // Verifique a mensagem esperada após a correção no AccountService
        assertEquals("Account with number " + existingAccountNumber + " already exists.", exception.getMessage());

        // Verify
        verify(accountRepository).existsByAccountNumber(existingAccountNumber);
        verify(accountRepository, never()).save(any(Account.class));
        verifyNoMoreInteractions(accountRepository);
        verifyNoInteractions(customerRepository);
    }

    @Test
    void deleteAccount_CallRepositoryDeleteById() {
        // Given
        when(accountRepository.existsById(accountId)).thenReturn(true); // Mock para o existsById dentro do serviço
        doNothing().when(accountRepository).deleteById(accountId);

        // When
        accountService.deleteAccount(accountId);

        // Then
        verify(accountRepository).existsById(accountId);
        verify(accountRepository).deleteById(accountId);
        verifyNoMoreInteractions(accountRepository); // Limpa interações que não deveriam ocorrer
        verifyNoInteractions(customerRepository);
    }

    @Test
    void updateAccount_shouldUpdateAndReturnAccount() {
        // Arrange
        AccountDTO updatedInfo = AccountDTO.builder()
                .accountNumber("12345-updated")
                .agency("001-updated")
                .balance(BigDecimal.ONE)
                .accountType(Account.AccountType.POUPANCA)
                .accountStatus(Account.AccountStatus.ATIVA)
                .build();

        Account existingAccountEntity = this.account;

        when(accountRepository.findById(this.accountId)).thenReturn(Optional.of(existingAccountEntity));
        when(accountRepository.findByAccountNumber(updatedInfo.getAccountNumber()))
                .thenReturn(Optional.empty());

        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Account actualUpdatedAccountEntity = accountService.updateAccount(this.accountId, updatedInfo);

        // Assert
        assertNotNull(actualUpdatedAccountEntity);
        assertEquals(this.accountId, actualUpdatedAccountEntity.getId(), "Account ID should remain unchanged.");
        assertEquals(updatedInfo.getAccountNumber(), actualUpdatedAccountEntity.getAccountNumber());
        assertEquals(updatedInfo.getAgency(), actualUpdatedAccountEntity.getAgency());
        assertEquals(0, updatedInfo.getBalance().compareTo(actualUpdatedAccountEntity.getBalance()));
        assertEquals(updatedInfo.getAccountType(), actualUpdatedAccountEntity.getAccountType());
        assertEquals(updatedInfo.getAccountStatus(), actualUpdatedAccountEntity.getAccountStatus());
        assertNotNull(actualUpdatedAccountEntity.getCustomer(), "Customer should not be null.");
        assertEquals(this.account.getCustomer(), actualUpdatedAccountEntity.getCustomer(), "Customer should remain the same as the original account's customer.");

        // Verify
        verify(accountRepository).findById(this.accountId);
        verify(accountRepository).findByAccountNumber(updatedInfo.getAccountNumber());
        verify(accountRepository).save(argThat(savedAccount ->
                savedAccount.getId().equals(this.accountId) &&
                savedAccount.getAccountNumber().equals(updatedInfo.getAccountNumber())
        ));
        verifyNoMoreInteractions(accountRepository);
        verifyNoInteractions(customerRepository);
    }

    @Test
    void updateAccount_whenAccountDoesNotExist_shouldThrowEntityNotFoundException() {
        // Given
        UUID nonExistentAccountId = UUID.randomUUID();
        AccountDTO updatedInfo = AccountDTO.builder()
                .accountNumber("98765")
                .agency("00X")
                .balance(new BigDecimal("500.00"))
                .accountType(Account.AccountType.INVESTIMENTO)
                .accountStatus(Account.AccountStatus.BLOQUEADA) // CORREÇÃO: BLOQUEADA (com E)
                .build();

        when(accountRepository.findById(nonExistentAccountId)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            accountService.updateAccount(nonExistentAccountId, updatedInfo);
        });

        assertEquals("Account with ID " + nonExistentAccountId + " does not exist.", exception.getMessage());

        // Verify
        verify(accountRepository).findById(nonExistentAccountId);
        verify(accountRepository, never()).save(any(Account.class));
        verify(accountRepository, never()).findByAccountNumber(anyString());
        verifyNoMoreInteractions(accountRepository);
        verifyNoInteractions(customerRepository);
    }

    @Test
    void accountExists_whenAccountExists_shouldReturnTrue() {
        when(accountRepository.existsById(accountId)).thenReturn(true);

        boolean exists = accountService.accountExists(accountId);

        assertTrue(exists);
        verify(accountRepository).existsById(accountId);
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    void accountExists_whenAccountDoesNotExist_shouldReturnFalse() {
        when(accountRepository.existsById(accountId)).thenReturn(false);

        boolean exists = accountService.accountExists(accountId);

        assertFalse(exists);
        verify(accountRepository).existsById(accountId);
        verifyNoMoreInteractions(accountRepository);
    }
}
