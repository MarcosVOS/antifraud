
package com.bradesco.antifraud.service;

import com.bradesco.antifraud.dto.AccountDto;
import com.bradesco.antifraud.exception.accountExceptions.AccountAlreadyExistsException;
import com.bradesco.antifraud.mapper.AccountMapper;
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

    @Mock
    private  AccountMapper accountMapper;


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
    void createAccount_AndReturnAccount() {
        // Arrange
        UUID customerId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        String accountNumber = "123456";

        AccountDto newAccountDto = AccountDto.builder()
                .accountNumber(accountNumber)
                .agency("001")
                .balance(BigDecimal.valueOf(1000))
                .accountType(Account.AccountType.CORRENTE)
                .accountStatus(Account.AccountStatus.ATIVA)
                .customerId(customerId)
                .build();

        Customer mockCustomer = Customer.builder().id(customerId).name("Test Customer").build();
        Account accountToSave = Account.builder()
                .accountNumber(newAccountDto.getAccountNumber())
                .agency(newAccountDto.getAgency())
                .balance(newAccountDto.getBalance())
                .accountType(newAccountDto.getAccountType())
                .accountStatus(newAccountDto.getAccountStatus())
                .customer(mockCustomer)
                .build(); // ID será definido pelo save

        Account savedAccount = Account.builder()
                .id(accountId) // Simula o ID gerado pelo banco
                .accountNumber(newAccountDto.getAccountNumber())
                .agency(newAccountDto.getAgency())
                .balance(newAccountDto.getBalance())
                .accountType(newAccountDto.getAccountType())
                .accountStatus(newAccountDto.getAccountStatus())
                .customer(mockCustomer)
                .build();

        when(accountRepository.existsByAccountNumber(accountNumber)).thenReturn(false);
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(mockCustomer));
        when(accountMapper.toEntity(newAccountDto)).thenReturn(accountToSave);
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

        // Act
        Account result = accountService.createAccount(newAccountDto);

        // Assert
        assertNotNull(result);
        assertEquals(savedAccount.getId(), result.getId());
        assertEquals(accountNumber, result.getAccountNumber());
        assertEquals(newAccountDto.getAgency(), result.getAgency());
        assertEquals(0, newAccountDto.getBalance().compareTo(result.getBalance()));
        assertEquals(newAccountDto.getAccountType(), result.getAccountType());
        assertEquals(newAccountDto.getAccountStatus(), result.getAccountStatus());
        assertNotNull(result.getCustomer());
        assertEquals(customerId, result.getCustomer().getId());

        verify(accountRepository).existsByAccountNumber(accountNumber);
        verify(customerRepository).findById(customerId);
        verify(accountMapper).toEntity(newAccountDto);
        verify(accountRepository).save(accountToSave);
    }

    @Test
    void createAccount_whenAccountNumberAlreadyExists_shouldThrowAccountAlreadyExistsException() {
        // Arrange
        String existingAccountNumber = "123456789";
        UUID customerId = UUID.randomUUID();

        AccountDto newAccountDto = AccountDto.builder()
                .accountNumber(existingAccountNumber)
                .agency("002")
                .balance(BigDecimal.valueOf(500))
                .accountType(Account.AccountType.CORRENTE)
                .accountStatus(Account.AccountStatus.ATIVA)
                .customerId(customerId)
                .build();

        // Mock para simular que uma conta com este número já existe
        when(accountRepository.existsByAccountNumber(existingAccountNumber)).thenReturn(true);

        // Act & Assert
        AccountAlreadyExistsException exception = assertThrows(AccountAlreadyExistsException.class, () -> {
            accountService.createAccount(newAccountDto);
        });

        assertEquals("Account with Id " + newAccountDto.getId() + " already exists.", exception.getMessage());

        // Verify
        verify(accountRepository).existsByAccountNumber(existingAccountNumber);
        verify(customerRepository, never()).findById(any(UUID.class));
        verify(accountMapper, never()).toEntity(any(AccountDto.class));
        verify(accountRepository, never()).save(any(Account.class));
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

        AccountDto updatedInfo = AccountDto.builder()
                .accountNumber("12345")
                .agency("001")
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

        AccountDto updatedInfoWithCustomer = AccountDto.builder()
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
