package com.bradesco.antifraud.controller;

import com.bradesco.antifraud.dto.AccountDTO;

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
public ResponseEntity<AccountDTO> getAccountByID(@PathVariable String id) {
    return accountService.getAccountById(UUID.fromString(id))
            .map(accountMapper::toDTO)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
}
    //Create a new Account
    @PostMapping("/newaccount")
public ResponseEntity<AccountDTO> createAccount(@RequestBody @Valid AccountDTO accountDTO) {
        
        Account accountEntity = accountMapper.toEntity(accountDTO);

        Account createdAccount = accountService.createAccount(accountEntity);
        AccountDTO createdAccountDTO = accountMapper.toDTO(createdAccount);


        URI location = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(createdAccountDTO.getId())
                .toUri();

        return ResponseEntity.created(location).body(createdAccountDTO);
}
    //Delete an Account
    @DeleteMapping("/deleteAccount/{id}")
public ResponseEntity<Void> deleteAccount(@PathVariable String id) {
    UUID accountId = UUID.fromString(id);
    if (!accountService.accountExists(accountId)) {
        return ResponseEntity.notFound().build();
    }
    accountService.deleteAccount(accountId);
    return ResponseEntity.noContent().build();
}

    //Update an account
    @PutMapping("/updateAccount/{id}")
public ResponseEntity<AccountDTO> updateAccount(@PathVariable String id, @RequestBody @Valid AccountDTO accountDTO) {
    UUID accountId = UUID.fromString(id);
    if (!accountService.accountExists(accountId)) {
        return ResponseEntity.notFound().build();
    }
    Account accountEntity = accountMapper.toEntity(accountDTO);
    accountEntity.setId(accountId);
    Account updatedAccount = accountService.updateAccount(accountId, accountEntity);
    AccountDTO updatedAccountDTO = accountMapper.toDTO(updatedAccount);
    
    return ResponseEntity.ok(updatedAccountDTO);
}
}