/*
package com.bradesco.antifraud.service;

import com.bradesco.antifraud.exception.accountExceptions.AccountAlreadyExistsException;
import com.bradesco.antifraud.model.Account;
import com.bradesco.antifraud.model.Address;
import com.bradesco.antifraud.model.Customer; // Supondo que Customer exista
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

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
        account = Account.builder()
                .id(accountId)
                .accountNumber("12345")
                .agency("001")
                .balance(BigDecimal.TEN)
                .accountType(Account.AccountType.CORRENTE)
                .accountStatus(Account.AccountStatus.ATIVA)
                .customer(createMockCustomer(customerId)) // Supondo que Account tenha um campo Customer
                .build();
    }

    @Test
    void getAccountById_whenAccountExists_shouldReturnAccount() {
        // Given
        when(accountRepository.existsById(accountId)).thenReturn(true); // Adicionado para cobrir a lógica do serviço
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        // When
        Optional<Account> foundAccount = accountService.getAccountById(accountId);

        // Then
        assertTrue(foundAccount.isPresent());
        assertEquals(account, foundAccount.get());
        verify(accountRepository).existsById(accountId);
        verify(accountRepository).findById(accountId);
    }


    @Test
    void getAccountById_whenAccountDoesNotExist_shouldThrowEntityNotFoundException() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(accountRepository.existsById(nonExistentId)).thenReturn(false);

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            accountService.getAccountById(nonExistentId);
        });
        assertEquals("Account with ID " + nonExistentId + " does not exist.", exception.getMessage());
        verify(accountRepository).existsById(nonExistentId);
        verify(accountRepository, never()).findById(nonExistentId);
    }

    @Test
    void createAccount_shouldSaveAndReturnAccount() {
        // Arrange
        String newAccountNumber = "67890";
        UUID generatedAccountId = UUID.randomUUID(); // ID simulado para a conta salva

        // Detalhes da conta a ser criada. O ID é nulo e o cliente já está associado.
        Account accountDetailsToCreate = Account.builder()
                .accountNumber(newAccountNumber)
                .agency("002")
                .balance(new BigDecimal("200.00"))
                .accountType(Account.AccountType.POUPANCA)
                .accountStatus(Account.AccountStatus.ATIVA)
                .customer(createMockCustomer(customerId)) // Cliente definido no setUp
                .build();

        // Conta esperada após ser salva (com ID gerado)
        Account expectedSavedAccount = Account.builder()
                .id(generatedAccountId)
                .accountNumber(newAccountNumber)
                .agency("002")
                .balance(new BigDecimal("200.00"))
                .accountType(Account.AccountType.POUPANCA)
                .accountStatus(Account.AccountStatus.ATIVA)
                .customer(createMockCustomer(customerId))
                .build();

        // Mocking:
        // 1. findByAccountNumber retorna false (número de conta é único)
        when(accountRepository.findByAccountNumber(newAccountNumber)).thenReturn(false);
        // 2. save é chamado com a conta (sem ID, com cliente) e retorna a conta com ID
        when(accountRepository.save(argThat(accToSave ->
                accToSave.getAccountNumber().equals(newAccountNumber) &&
                        accToSave.getCustomer().equals(createMockCustomer(customerId)) &&
                        accToSave.getId() == null // Verifica se o ID é nulo antes de salvar
        ))).thenReturn(expectedSavedAccount);

        // Act
        // O parâmetro this.customerId é passado, mas o método createAccount atual não o utiliza.
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
        verify(accountRepository).findByAccountNumber(newAccountNumber);
        verify(accountRepository).save(argThat(accToSave ->
                accToSave.getAccountNumber().equals(newAccountNumber) &&
                        accToSave.getCustomer().equals(createMockCustomer(customerId)) &&
                        accToSave.getId() == null
        ));
        verifyNoMoreInteractions(accountRepository);
        verifyNoInteractions(customerRepository); // customerRepository não é usado pelo createAccount atual
    }

    @Test
    void createAccount_whenAccountNumberAlreadyExists_shouldThrowAccountAlreadyExistsException() {
        // Arrange
        String existingAccountNumber = "12345"; // Número de conta que simula existência

        Account accountAttemptingToCreate = Account.builder()
                .accountNumber(existingAccountNumber)
                .agency("003")
                .balance(new BigDecimal("50.00"))
                .accountType(Account.AccountType.CORRENTE)
                .accountStatus(Account.AccountStatus.ATIVA)
                .customer(createMockCustomer(customerId)) // Cliente do setUp
                .build(); // ID é nulo

        // Mocking: findByAccountNumber retorna true (conta já existe)
        when(accountRepository.findByAccountNumber(existingAccountNumber)).thenReturn(true);

        // Act & Assert
        AccountAlreadyExistsException exception = assertThrows(AccountAlreadyExistsException.class, () -> {
            accountService.createAccount(accountAttemptingToCreate);
        });

        // Verifica a mensagem da exceção (baseada na implementação atual do serviço)
        assertEquals("Account with Id null already exists.", exception.getMessage());


        // Verify
        verify(accountRepository).findByAccountNumber(existingAccountNumber);
        verify(accountRepository, never()).save(any(Account.class)); // Garante que save não foi chamado
        verifyNoMoreInteractions(accountRepository);
        verifyNoInteractions(customerRepository);
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
                .customer(createMockCustomer(customerId))
                .build(); // ID não é necessário aqui, será setado pelo serviço

        Account expectedUpdatedAccount = Account.builder()
                .id(accountId) // ID é mantido
                .accountNumber("12345-updated")
                .agency("001-updated")
                .balance(BigDecimal.ONE)
                .accountType(Account.AccountType.POUPANCA)
                .accountStatus(Account.AccountStatus.ATIVA)
                .customer(createMockCustomer(customerId))
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
    void updateAccount_whenAccountDoesNotExist_shouldThrowEntityNotFoundException() {
        UUID nonExistentId = UUID.randomUUID();
        Account updatedInfo = Account.builder().build();

        when(accountRepository.existsById(nonExistentId)).thenReturn(false);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
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
*/