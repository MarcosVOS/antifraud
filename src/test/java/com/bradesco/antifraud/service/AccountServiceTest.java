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
                .address(address) // Atribui o objeto Address diretamente
                .password("securePassword123")
                .build();
    }

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        Customer mockCustomer = createMockCustomer(customerId);

        account = Account.builder()
                .id(accountId)
                .accountNumber("12345")
                .agency("001")
                .balance(BigDecimal.TEN)
                .accountType(Account.AccountType.CORRENTE)
                .accountStatus(Account.AccountStatus.ATIVA)
                .customer(mockCustomer)
                .build();
    }

    @Test
    void getAccountById_whenAccountExists_shouldReturnAccount() {
        when(accountRepository.existsById(accountId)).thenReturn(true);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        Optional<Account> foundAccount = accountService.getAccountById(accountId);

        assertTrue(foundAccount.isPresent());
        assertEquals(account, foundAccount.get());
        verify(accountRepository).existsById(accountId);
        verify(accountRepository).findById(accountId);
    }

    @Test
    void getAccountById_whenAccountDoesNotExist_shouldThrowEntityNotFoundException() {
        UUID nonExistentId = UUID.randomUUID();
        when(accountRepository.existsById(nonExistentId)).thenReturn(false);

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            accountService.getAccountById(nonExistentId);
        });
        assertEquals("Account with ID " + nonExistentId + " does not exist.", exception.getMessage());
        verify(accountRepository).existsById(nonExistentId);
        verify(accountRepository, never()).findById(nonExistentId);
    }

    @Test
    void createAccount_shouldSaveAndReturnAccount() {
        String newAccountNumber = "67890";
        UUID generatedAccountId = UUID.randomUUID();

        Customer stubCustomerInNewAccount = new Customer();
        stubCustomerInNewAccount.setId(customerId);

        Account accountDetailsToCreate = Account.builder()
                .accountNumber(newAccountNumber)
                .agency("002")
                .balance(new BigDecimal("200.00"))
                .accountType(Account.AccountType.POUPANCA)
                .accountStatus(Account.AccountStatus.ATIVA)
                .customer(stubCustomerInNewAccount)
                .build();

        Customer realCustomer = createMockCustomer(customerId);
        Account expectedSavedAccount = Account.builder()
                .id(generatedAccountId)
                .accountNumber(newAccountNumber)
                .agency("002")
                .balance(new BigDecimal("200.00"))
                .accountType(Account.AccountType.POUPANCA)
                .accountStatus(Account.AccountStatus.ATIVA)
                .customer(realCustomer)
                .build();

        when(accountRepository.existsByAccountNumber(newAccountNumber)).thenReturn(false);
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(realCustomer));
        when(accountRepository.save(any(Account.class))).thenReturn(expectedSavedAccount);

        Account actualSavedAccount = accountService.createAccount(accountDetailsToCreate);

        assertNotNull(actualSavedAccount, "A conta salva não deve ser nula.");
        assertEquals(expectedSavedAccount.getId(), actualSavedAccount.getId(), "O ID da conta salva está incorreto.");
        assertEquals(expectedSavedAccount.getAccountNumber(), actualSavedAccount.getAccountNumber(), "O número da conta está incorreto.");
        assertEquals(expectedSavedAccount.getCustomer().getId(), actualSavedAccount.getCustomer().getId(), "O ID do cliente associado está incorreto.");
        assertEquals(realCustomer.getName(), actualSavedAccount.getCustomer().getName());

        verify(accountRepository).existsByAccountNumber(newAccountNumber);
        verify(customerRepository).findById(customerId);
        verify(accountRepository).save(argThat(accToSave ->
                accToSave.getAccountNumber().equals(newAccountNumber) &&
                accToSave.getCustomer().getId().equals(customerId) &&
                accToSave.getId() == null
        ));
        verifyNoMoreInteractions(accountRepository, customerRepository);
    }

    @Test
    void createAccount_whenAccountNumberAlreadyExists_ThrowAccountAlreadyExistsException() {
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

        AccountAlreadyExistsException exception = assertThrows(AccountAlreadyExistsException.class, () -> {
            accountService.createAccount(accountAttemptingToCreate);
        });

        assertEquals("Account with number " + existingAccountNumber + " already exists.", exception.getMessage());

        verify(accountRepository).existsByAccountNumber(existingAccountNumber);
        verify(accountRepository, never()).save(any(Account.class));
        verifyNoInteractions(customerRepository);
    }

    @Test
    void createAccount_whenCustomerDoesNotExist_shouldThrowEntityNotFoundException() {
        String newAccountNumber = "112233";
        UUID nonExistentCustomerId = UUID.randomUUID();

        Customer stubCustomerInNewAccount = new Customer();
        stubCustomerInNewAccount.setId(nonExistentCustomerId);

        Account accountDetailsToCreate = Account.builder()
                .accountNumber(newAccountNumber)
                .agency("004")
                .balance(new BigDecimal("300.00"))
                .accountType(Account.AccountType.INVESTIMENTO)
                .accountStatus(Account.AccountStatus.ATIVA)
                .customer(stubCustomerInNewAccount)
                .build();

        when(accountRepository.existsByAccountNumber(newAccountNumber)).thenReturn(false);
        when(customerRepository.findById(nonExistentCustomerId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            accountService.createAccount(accountDetailsToCreate);
        });

        assertEquals("Customer with ID " + nonExistentCustomerId + " does not exist.", exception.getMessage());

        verify(accountRepository).existsByAccountNumber(newAccountNumber);
        verify(customerRepository).findById(nonExistentCustomerId);
        verify(accountRepository, never()).save(any(Account.class));
        verifyNoMoreInteractions(accountRepository, customerRepository);
    }

    @Test
    void deleteAccount_CallRepositoryDeleteById() {
        when(accountRepository.existsById(accountId)).thenReturn(true);

        doNothing().when(accountRepository).deleteById(accountId);

        accountService.deleteAccount(accountId);

        verify(accountRepository).existsById(accountId);
        verify(accountRepository).deleteById(accountId);

        verifyNoMoreInteractions(accountRepository);
        verifyNoInteractions(customerRepository);
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
                .customerId(newCustomerIdForUpdate)
                .build();

        Account existingAccountEntity = this.account;

        when(accountRepository.findById(this.accountId)).thenReturn(Optional.of(existingAccountEntity));
        when(accountRepository.findByAccountNumber(updatedInfo.getAccountNumber()))
                .thenReturn(Optional.empty());
        when(customerRepository.findById(newCustomerIdForUpdate)).thenReturn(Optional.of(newMockCustomer));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Account actualUpdatedAccountEntity = accountService.updateAccount(this.accountId, updatedInfo);

        assertNotNull(actualUpdatedAccountEntity);
        assertEquals(this.accountId, actualUpdatedAccountEntity.getId(), "Account ID should remain unchanged.");
        assertEquals(updatedInfo.getAccountNumber(), actualUpdatedAccountEntity.getAccountNumber());
        assertEquals(updatedInfo.getAgency(), actualUpdatedAccountEntity.getAgency());
        assertEquals(0, updatedInfo.getBalance().compareTo(actualUpdatedAccountEntity.getBalance()));
        assertEquals(updatedInfo.getAccountType(), actualUpdatedAccountEntity.getAccountType());
        assertEquals(updatedInfo.getAccountStatus(), actualUpdatedAccountEntity.getAccountStatus());
        assertNotNull(actualUpdatedAccountEntity.getCustomer(), "Customer should not be null.");
        assertEquals(newMockCustomer.getId(), actualUpdatedAccountEntity.getCustomer().getId(), "Customer ID should be updated.");
        assertEquals(newMockCustomer.getName(), actualUpdatedAccountEntity.getCustomer().getName(), "Customer details should reflect the new customer.");

        verify(accountRepository).findById(this.accountId);
        verify(accountRepository).findByAccountNumber(updatedInfo.getAccountNumber());
        verify(customerRepository).findById(newCustomerIdForUpdate);
        verify(accountRepository).save(argThat(savedAccount ->
                savedAccount.getId().equals(this.accountId) &&
                savedAccount.getAccountNumber().equals(updatedInfo.getAccountNumber()) &&
                savedAccount.getCustomer().getId().equals(newCustomerIdForUpdate)
        ));
        verifyNoMoreInteractions(accountRepository, customerRepository);
    }

    @Test
    void updateAccount_whenAccountDoesNotExist_shouldThrowEntityNotFoundException() {
        UUID nonExistentAccountId = UUID.randomUUID();
        UUID mockCustomerIdForUpdate = UUID.randomUUID();

        AccountDTO updatedInfoWithCustomer = AccountDTO.builder()
                .accountNumber("98765")
                .agency("00X")
                .balance(new BigDecimal("500.00"))
                .accountType(Account.AccountType.INVESTIMENTO)
                .accountStatus(Account.AccountStatus.BLOQUADA)
                .customerId(mockCustomerIdForUpdate)
                .build();

        when(accountRepository.findById(nonExistentAccountId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            accountService.updateAccount(nonExistentAccountId, updatedInfoWithCustomer);
        });

        assertEquals("Account with ID " + nonExistentAccountId + " does not exist.", exception.getMessage());

        verify(accountRepository).findById(nonExistentAccountId);
        verify(accountRepository, never()).save(any(Account.class));
        verify(customerRepository, never()).findById(any(UUID.class));
        verifyNoMoreInteractions(accountRepository, customerRepository);
    }

    @Test
    void updateAccount_whenNewAccountNumberAlreadyExistsForAnotherAccount_shouldThrowAccountAlreadyExistsException() {
        UUID existingAccountId = UUID.randomUUID();
        Account existingAccount = Account.builder()
                .id(existingAccountId)
                .accountNumber("existingAccNum")
                .agency("001")
                .balance(BigDecimal.valueOf(500))
                .accountType(Account.AccountType.CORRENTE)
                .accountStatus(Account.AccountStatus.ATIVA)
                .customer(createMockCustomer(UUID.randomUUID()))
                .build();

        AccountDTO updatedInfo = AccountDTO.builder()
                .accountNumber("existingAccNum")
                .agency("002")
                .balance(BigDecimal.valueOf(1000))
                .accountType(Account.AccountType.POUPANCA)
                .accountStatus(Account.AccountStatus.ATIVA)
                .customerId(customerId)
                .build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(this.account));
        when(accountRepository.findByAccountNumber(updatedInfo.getAccountNumber()))
                .thenReturn(Optional.of(existingAccount));

        AccountAlreadyExistsException exception = assertThrows(AccountAlreadyExistsException.class, () -> {
            accountService.updateAccount(accountId, updatedInfo);
        });

        assertEquals("Account with number " + updatedInfo.getAccountNumber() + " already exists.", exception.getMessage());

        verify(accountRepository).findById(accountId);
        verify(accountRepository).findByAccountNumber(updatedInfo.getAccountNumber());
        verify(accountRepository, never()).save(any(Account.class));
        verifyNoMoreInteractions(accountRepository, customerRepository);
    }

    @Test
    void updateAccount_whenNewCustomerDoesNotExist_shouldThrowEntityNotFoundException() {
        UUID nonExistentNewCustomerId = UUID.randomUUID();
        AccountDTO updatedInfo = AccountDTO.builder()
                .accountNumber("12345-updated")
                .agency("001-updated")
                .balance(BigDecimal.ONE)
                .accountType(Account.AccountType.POUPANCA)
                .accountStatus(Account.AccountStatus.ATIVA)
                .customerId(nonExistentNewCustomerId)
                .build();

        Account existingAccountEntity = this.account;

        when(accountRepository.findById(this.accountId)).thenReturn(Optional.of(existingAccountEntity));
        when(accountRepository.findByAccountNumber(updatedInfo.getAccountNumber())).thenReturn(Optional.empty());
        when(customerRepository.findById(nonExistentNewCustomerId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            accountService.updateAccount(this.accountId, updatedInfo);
        });

        assertEquals("Customer with ID " + nonExistentNewCustomerId + " does not exist.", exception.getMessage());

        verify(accountRepository).findById(this.accountId);
        verify(accountRepository).findByAccountNumber(updatedInfo.getAccountNumber());
        verify(customerRepository).findById(nonExistentNewCustomerId);
        verify(accountRepository, never()).save(any(Account.class));
        verifyNoMoreInteractions(accountRepository, customerRepository);
    }

    @Test
    void accountExists_whenAccountExists_shouldReturnTrue() {
        when(accountRepository.existsById(accountId)).thenReturn(true);

        boolean exists = accountService.accountExists(accountId);

        assertTrue(exists);
        verify(accountRepository).existsById(accountId);
        verifyNoMoreInteractions(accountRepository);
        verifyNoInteractions(customerRepository);
    }

    @Test
    void accountExists_whenAccountDoesNotExist_shouldReturnFalse() {
        when(accountRepository.existsById(accountId)).thenReturn(false);

        boolean exists = accountService.accountExists(accountId);

        assertFalse(exists);
        verify(accountRepository).existsById(accountId);
        verifyNoMoreInteractions(accountRepository);
        verifyNoInteractions(customerRepository);
    }
}
