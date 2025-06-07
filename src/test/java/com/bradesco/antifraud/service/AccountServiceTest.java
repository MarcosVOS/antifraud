
package com.bradesco.antifraud.service;

import com.bradesco.antifraud.dto.AccountDTO;
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
        when(accountRepository.existsByAccountNumber(newAccountNumber)).thenReturn(false);
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
        verify(accountRepository).existsByAccountNumber(newAccountNumber);
        verify(accountRepository).save(argThat(accToSave ->
                accToSave.getAccountNumber().equals(newAccountNumber) &&
                        accToSave.getCustomer().equals(createMockCustomer(customerId)) &&
                        accToSave.getId() == null
        ));
        verifyNoMoreInteractions(accountRepository);
        verifyNoInteractions(customerRepository); // customerRepository não é usado pelo createAccount atual
    }

    @Test
    void createAccount_whenAccountNumberAlreadyExists_ThrowAccountAlreadyExistsException() {
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
        when(accountRepository.existsByAccountNumber(existingAccountNumber)).thenReturn(true);

        // Act & Assert
        AccountAlreadyExistsException exception = assertThrows(AccountAlreadyExistsException.class, () -> {
            accountService.createAccount(accountAttemptingToCreate);
        });

        // Verifica a mensagem da exceção (baseada na implementação atual do serviço)
        assertEquals("Account with Id null already exists.", exception.getMessage());


        // Verify
        verify(accountRepository).existsByAccountNumber(existingAccountNumber);
        verify(accountRepository, never()).save(any(Account.class)); // Garante que save não foi chamado
        verifyNoMoreInteractions(accountRepository);
        verifyNoInteractions(customerRepository);
    }

    @Test
    void deleteAccount_CallRepositoryDeleteById() {
     
        // Mocking the check for account existence, assuming your service method does this.
        when(accountRepository.existsById(accountId)).thenReturn(true);
       
        doNothing().when(accountRepository).deleteById(accountId);

        accountService.deleteAccount(accountId);

       
        // Verify that the existence check was performed
        verify(accountRepository).existsById(accountId);
        // Verify that the account deletion was called on the repository
        verify(accountRepository).deleteById(accountId);

        // Verify that the customer associated with this account was NOT deleted.
        // 'this.customerId' is the ID of the customer associated with 'this.account' in setUp.
        verify(customerRepository, never()).deleteById(this.customerId);
    }

    @Test
    void updateAccountshouldUpdateAndReturnAccount() {
        UUID newCustomerIdForUpdate = UUID.randomUUID();
        Customer newMockCustomer = createMockCustomer(newCustomerIdForUpdate);

        AccountDTO updatedInfo = AccountDTO.builder()
                .accountNumber("12345-updated")
                .agency("001-updated")
                .balance(BigDecimal.ONE)
                .accountType(Account.AccountType.POUPANCA)
                .accountStatus(Account.AccountStatus.ATIVA)
              //  .customerId(newCustomerIdForUpdate) // Novo ID de cliente para atualização
                .build();

        Account existingAccountEntity = this.account; 

        // Mocking:
        // 1. Serviço vai chamar findById para carregar a conta existente
        when(accountRepository.findById(this.accountId)).thenReturn(Optional.of(existingAccountEntity));

        // 2. Serviço vai verificar se o novo número de conta já existe (para outra conta)
        when(accountRepository.findByAccountNumber(updatedInfo.getAccountNumber()))
                .thenReturn(Optional.empty()); // Assume que o novo número não conflita


        // 3. Serviço vai salvar a entidade atualizada.
        // Usamos thenAnswer para retornar a entidade que foi passada para save,
        // pois ela já terá sido modificada pelo serviço.
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
        assertEquals(newMockCustomer.getName(), actualUpdatedAccountEntity.getCustomer().getName(), "Customer details should reflect the new customer.");


        // Verify
        verify(accountRepository).findById(this.accountId);
        verify(accountRepository).findByAccountNumber(updatedInfo.getAccountNumber());
        verify(accountRepository).save(argThat(savedAccount ->
            savedAccount.getId().equals(this.accountId) &&
            savedAccount.getAccountNumber().equals(updatedInfo.getAccountNumber())
        ));
    }

    @Test
    void updateAccount_whenAccountDoesNotExist_shouldThrowEntityNotFoundException() {
          // Given
        UUID nonExistentAccountId = UUID.randomUUID();
        UUID mockCustomerIdForUpdate = UUID.randomUUID(); // A different ID for clarity if needed
        Customer customerDetailsForUpdate = createMockCustomer(mockCustomerIdForUpdate);

        AccountDTO updatedInfoWithCustomer = AccountDTO.builder()
                .accountNumber("98765") // Example data
                .agency("00X")
                .balance(new BigDecimal("500.00"))
                .accountType(Account.AccountType.INVESTIMENTO)
                .accountStatus(Account.AccountStatus.BLOQUADA)
                .customerId(customerDetailsForUpdate.getId()) // Include mock customer in the update data
                .build();

        // Mock the repository to indicate the account does not exist
        when(accountRepository.findById(nonExistentAccountId)).thenReturn(Optional.empty());


         EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            accountService.updateAccount(nonExistentAccountId, updatedInfoWithCustomer);
        });

        // Assert the exception message is correct
        assertEquals("Account with ID " + nonExistentAccountId + " does not exist.", exception.getMessage());

        // Verify
        // Ensure the check for account existence was made
        verify(accountRepository).findById(nonExistentAccountId);
        verify(accountRepository, never()).save(any(Account.class));
        verify(customerRepository, never()).save(any(Customer.class));
        verify(customerRepository, never()).findById(any(UUID.class));
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
