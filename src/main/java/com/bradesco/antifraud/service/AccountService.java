package com.bradesco.antifraud.service;

import com.bradesco.antifraud.dto.AccountDTO;
import com.bradesco.antifraud.exception.accountExceptions.AccountAlreadyExistsException;
import com.bradesco.antifraud.model.Account;
import com.bradesco.antifraud.model.Customer;
import com.bradesco.antifraud.repository.AccountRepository;
import com.bradesco.antifraud.repository.CustomerRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    public AccountService(AccountRepository accountRepository,
                          CustomerRepository customerRepository) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
    }

    public Optional<Account> getAccountById(UUID id) {
        if (!accountRepository.existsById(id)) {
            throw new EntityNotFoundException("Account with ID " + id + " does not exist.");
        }
        return accountRepository.findById(id);
    }

    @Transactional
    public Account createAccount(Account newAccount) {
        if (accountRepository.existsByAccountNumber((newAccount.getAccountNumber()))) {
            throw new AccountAlreadyExistsException("Account with number " + newAccount.getAccountNumber() + " already exists.");
        }

        if (newAccount.getCustomer() != null && newAccount.getCustomer().getId() != null) {
            Customer customer = customerRepository.findById(newAccount.getCustomer().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Customer with ID " + newAccount.getCustomer().getId() + " does not exist."));
            newAccount.setCustomer(customer);
        } else {
            throw new IllegalArgumentException("Customer ID is required to create an account.");
        }

        return accountRepository.save(newAccount);
    }

    public void deleteAccount(UUID id) {
        if (!accountRepository.existsById(id)) {
            throw new EntityNotFoundException("Account with ID " + id + " does not exist.");
        }
        accountRepository.deleteById(id);
    }

    @Transactional
    public Account updateAccount(UUID id, AccountDTO updatedAccountData) {
        Account existingAccount = accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Account with ID " + id + " does not exist."));

        Optional<Account> accountByNewNumber = accountRepository.findByAccountNumber(updatedAccountData.getAccountNumber());
        if (accountByNewNumber.isPresent() && !accountByNewNumber.get().getId().equals(id)) {
            throw new AccountAlreadyExistsException("Account with number " + updatedAccountData.getAccountNumber() + " already exists.");
        }

        existingAccount.setAccountNumber(updatedAccountData.getAccountNumber());
        existingAccount.setAgency(updatedAccountData.getAgency());
        existingAccount.setBalance(updatedAccountData.getBalance());
        existingAccount.setAccountType(updatedAccountData.getAccountType());
        existingAccount.setAccountStatus(updatedAccountData.getAccountStatus());

        if (updatedAccountData.getCustomerId() != null &&
            !updatedAccountData.getCustomerId().equals(existingAccount.getCustomer().getId())) {
            Customer newCustomer = customerRepository.findById(updatedAccountData.getCustomerId())
                    .orElseThrow(() -> new EntityNotFoundException("Customer with ID " + updatedAccountData.getCustomerId() + " does not exist."));
            existingAccount.setCustomer(newCustomer);
        } else if (updatedAccountData.getCustomerId() == null) {
             throw new IllegalArgumentException("Customer ID cannot be null for account update.");
        }

        return accountRepository.save(existingAccount);
    }

    public boolean accountExists(UUID id) {
        return accountRepository.existsById(id);
    }
}
