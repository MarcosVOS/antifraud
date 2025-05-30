package com.bradesco.antifraud.controller;

import com.bradesco.antifraud.model.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
class AccountController {
    // Placeholder for future methods related to account operations
    // e.g., createAccount, getAccountById, updateAccount, deleteAccount, etc.
    @GetMapping("/{id}")
    public Account getAccountByID(@PathVariable String id) {
        // This method is a placeholder and should be implemented to retrieve an account by its ID.
        // For now, it returns a new Account object with the provided ID.
        return new Account();
    }

}
