package com.bradesco.antifraud.service;

import com.bradesco.antifraud.exception.accountExceptions.AccountAlreadyExistsException;
import com.bradesco.antifraud.model.Account;
import com.bradesco.antifraud.repository.AccountRepository;
import com.bradesco.antifraud.dto.AccountDTO;
// Remova CustomerRepository se não for usado neste serviço para evitar warnings
// Remova Address, Customer se não forem usados diretamente neste serviço para evitar warnings

import jakarta.persistence.EntityNotFoundException; // Mantenha este import se ainda usar em outros métodos
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository /*, CustomerRepository customerRepository */) {
        this.accountRepository = accountRepository;
    }

    public Optional<Account> findById(UUID id) {
        return accountRepository.findById(id);
    }

    public Account createAccount(Account newAccount) {
        if (accountRepository.existsByAccountNumber((newAccount.getAccountNumber()))) {
            throw new AccountAlreadyExistsException("Account with number " + newAccount.getAccountNumber() + " already exists.");
        }
        return accountRepository.save(newAccount);
    }

    public void deleteAccount(UUID id) {
        if (!accountRepository.existsById(id)) {
            throw new EntityNotFoundException("Account with ID " + id + " does not exist.");
        }
        accountRepository.deleteById(id);
    }

    public Account updateAccount(UUID id, AccountDTO accountDTO) {
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
        // Se você não planeja atualizar o customer através do AccountDTO, não inclua aqui.

        return accountRepository.save(existingAccount);
    }

    public boolean accountExists(UUID id) {
        return accountRepository.existsById(id);
    }
}
