package com.bradesco.antifraud.controller;

import com.bradesco.antifraud.dto.AccountDTO;

import com.bradesco.antifraud.mapper.AccountMapper;
import com.bradesco.antifraud.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
class AccountController {

   @Autowired
    private final AccountService accountService;


   @Autowired
    private final AccountMapper accountMapper;

    @GetMapping("/{id}")
public ResponseEntity<AccountDTO> getAccountByID(@PathVariable String id) {
    return accountService.getAccountById(UUID.fromString(id))
            .map(accountMapper::toDTO)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
}

}
