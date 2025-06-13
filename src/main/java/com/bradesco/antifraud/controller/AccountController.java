package com.bradesco.antifraud.controller;

import com.bradesco.antifraud.dto.AccountDto;
import com.bradesco.antifraud.dto.CreateAccountDTO;
import com.bradesco.antifraud.mapper.AccountMapper;
import com.bradesco.antifraud.model.Account;
import com.bradesco.antifraud.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
class AccountController {

    private final AccountService accountService;

    private final AccountMapper accountMapper;

    @GetMapping("/{id}")
    public ResponseEntity<AccountDto> getAccountByID(@PathVariable String id) {
        return accountService.getAccountById(UUID.fromString(id))
                .map(accountMapper::toDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Create a new Account
    @PostMapping("/newaccount")
    public ResponseEntity<CreateAccountDTO> createAccount(@RequestBody AccountDto newAccountDTO) {
        // Ensure the ID is null for creation

        Account newAccount = accountService.createAccount(newAccountDTO);
        CreateAccountDTO newAccountDto = accountMapper.newAccounttoDto(newAccount);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(newAccountDTO.getId())
                .toUri();

        return ResponseEntity.created(location).body(newAccountDto);
    }

    // Delete an Account
    @DeleteMapping("/deleteAccount/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable String id) {
        UUID accountId = UUID.fromString(id);
        if (!accountService.accountExists(accountId)) {
            return ResponseEntity.notFound().build();
        }
        accountService.deleteAccount(accountId);
        return ResponseEntity.noContent().build();
    }

    // Update an account
    @PutMapping("/updateAccount/{id}")
    public ResponseEntity<AccountDto> updateAccount(@PathVariable String id,
            @RequestBody @Valid AccountDto accountDTO) {
        UUID accountId = UUID.fromString(id);

        Account updatedAccount = accountService.updateAccount(accountId, accountDTO);
        AccountDto updatedAccountDTO = accountMapper.toDto(updatedAccount);

        return ResponseEntity.ok(updatedAccountDTO);
    }
}