package com.bradesco.antifraud.service;



import com.bradesco.antifraud.dto.AccountDto;

import com.bradesco.antifraud.exception.accountExceptions.AccountAlreadyExistsException;
import com.bradesco.antifraud.mapper.AccountMapper;
import com.bradesco.antifraud.model.Account;
import com.bradesco.antifraud.model.Customer;
import com.bradesco.antifraud.repository.AccountRepository;

import com.bradesco.antifraud.dto.AccountDTO;
// Remova CustomerRepository se não for usado neste serviço para evitar warnings
// Remova Address, Customer se não forem usados diretamente neste serviço para evitar warnings

import jakarta.persistence.EntityNotFoundException; // Mantenha este import se ainda usar em outros métodos

import com.bradesco.antifraud.repository.CustomerRepository;

import jakarta.persistence.EntityNotFoundException;


import jakarta.validation.constraints.NotNull;

import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final CustomerRepository customerRepository;

    public AccountService(AccountRepository accountRepository /*, CustomerRepository customerRepository */) {

    public AccountService(AccountRepository accountRepository, AccountMapper accountMapper, CustomerRepository customerRepository) {
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
        this.customerRepository = customerRepository;
    }

    public Optional<Account> findById(UUID id) {
        return accountRepository.findById(id);
    }

    public Account createAccount(Account newAccount) {


    public Account createAccount(@NotNull AccountDto newAccount) {

        if (accountRepository.existsByAccountNumber((newAccount.getAccountNumber()))) {
            throw new AccountAlreadyExistsException("Account with number " + newAccount.getAccountNumber() + " already exists.");
        }

        return accountRepository.save(newAccount);

        Account account = accountMapper.toEntity(newAccount);


        Customer customer = customerRepository.findById(newAccount.getCustomerId())
                  .orElseThrow(() -> new EntityNotFoundException("Customer with ID " + newAccount.getCustomerId() + " not found."));
        account.setCustomer(customer);

        return accountRepository.save(account);
    }

    public void deleteAccount(UUID id) {
        if (!accountRepository.existsById(id)) {
            throw new EntityNotFoundException("Account with ID " + id + " does not exist.");
        }
        accountRepository.deleteById(id);
    }


    public Account updateAccount(UUID id, AccountDTO accountDTO) {


    public Account updateAccount(UUID id, AccountDto updatedAccountData) { // Pode precisar do customerId se for alterável

        Account existingAccount = accountRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Account with ID " + id + " does not exist."));

        if (accountDTO.getAccountNumber() != null && !accountDTO.getAccountNumber().equals(existingAccount.getAccountNumber())) {
            if (accountRepository.findByAccountNumber(accountDTO.getAccountNumber()).isPresent()) {
                throw new AccountAlreadyExistsException("Account with number " + accountDTO.getAccountNumber() + " already exists.");
            }
        }


        existingAccount.setAccountNumber(accountDTO.getAccountNumber());
        existingAccount.setAgency(accountDTO.getAgency());
        existingAccount.setBalance(accountDTO.getBalance());
        existingAccount.setAccountType(accountDTO.getAccountType());
        existingAccount.setAccountStatus(accountDTO.getAccountStatus());


        if (updatedAccountData.getAgency() != null) {
        existingAccount.setAgency(updatedAccountData.getAgency());
    }
        if (updatedAccountData.getBalance() != null) {
        existingAccount.setBalance(updatedAccountData.getBalance());
    }
        if (updatedAccountData.getAccountType() != null) {
        existingAccount.setAccountType(updatedAccountData.getAccountType());
    }
        if (updatedAccountData.getAccountStatus() != null) {
        existingAccount.setAccountStatus(updatedAccountData.getAccountStatus());
    }


        return accountRepository.save(existingAccount);
    }

    public boolean accountExists(UUID id) {
        return accountRepository.existsById(id);
    }
}
