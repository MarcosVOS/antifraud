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

    @PostMapping("/newaccount")

public ResponseEntity<AccountDTO> createAccount(@RequestBody @Valid AccountDTO accountDTO) {
        Account accountEntity = accountMapper.toEntity(accountDTO);
        Account createdAccount = accountService.createAccount(accountEntity);
        AccountDTO createdAccountDTO = accountMapper.toDTO(createdAccount);


        URI location = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/newaccount")
                .buildAndExpand(createdAccountDTO.getId())
                .toUri();


        return ResponseEntity.created(location).body(createdAccountDTO);
}

}
