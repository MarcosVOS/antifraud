    package com.bradesco.antifraud.controller;


    import com.bradesco.antifraud.dto.AccountDTO;
    import com.bradesco.antifraud.exception.accountExceptions.AccountAlreadyExistsException;
    import com.bradesco.antifraud.mapper.AccountMapper;
    import com.bradesco.antifraud.model.Account;
    import com.bradesco.antifraud.service.AccountService;
    import jakarta.persistence.EntityNotFoundException;
    import jakarta.validation.Valid;
    import lombok.RequiredArgsConstructor;


import com.bradesco.antifraud.dto.AccountDto;
import com.bradesco.antifraud.dto.CreateAccountDTO;
import com.bradesco.antifraud.mapper.AccountMapper;
import com.bradesco.antifraud.model.Account;
import com.bradesco.antifraud.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


    import org.springframework.http.HttpStatus; // ADICIONADO: Importação para HttpStatus
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;
    import org.springframework.web.server.ResponseStatusException; // ADICIONADO: Importação para ResponseStatusException
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
            try {
                UUID accountId = UUID.fromString(id);
                // CORREÇÃO: Chamar findById em vez de getAccountById
                return accountService.findById(accountId) // <-- LINHA CORRIGIDA
                        .map(accountMapper::toDTO)
                        .map(ResponseEntity::ok)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Conta com ID " + id + " não encontrada."));
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato de ID de conta inválido: " + id + ". Deve ser um UUID.", e);
            }
        }
        //Create a new Account
        @PostMapping("/newaccount")
        public ResponseEntity<AccountDTO> createAccount(@RequestBody @Valid AccountDTO accountDTO) {
            
            try { // Bloco try-catch adicionado para as exceções do serviço
                Account accountEntity = accountMapper.toEntity(accountDTO);


                Account createdAccount = accountService.createAccount(accountEntity);
                AccountDTO createdAccountDTO = accountMapper.toDTO(createdAccount);



    @GetMapping("/{id}")
public ResponseEntity<AccountDto> getAccountByID(@PathVariable String id) {
    return accountService.getAccountById(UUID.fromString(id))
            .map(accountMapper::toDto)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
}
    //Create a new Account
    @PostMapping("/newaccount")
public ResponseEntity<CreateAccountDTO> createAccount(@RequestBody AccountDto newAccountDTO) {
        // Ensure the ID is null for creation

        Account newAccount = accountService.createAccount(newAccountDTO);
        CreateAccountDTO newAccountDto = accountMapper.newAccounttoDto(newAccount);


                URI location = ServletUriComponentsBuilder
                        .fromCurrentRequestUri()
                        .path("/{id}")
                        .buildAndExpand(createdAccountDTO.getId())
                        .toUri();


                return ResponseEntity.created(location).body(createdAccountDTO);
            } catch (AccountAlreadyExistsException e) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage(), e);
            }
        }
        //Delete an Account
        @DeleteMapping("/deleteAccount/{id}")
        public ResponseEntity<Void> deleteAccount(@PathVariable String id) {
            try { // Bloco try-catch adicionado para as exceções do serviço
                UUID accountId = UUID.fromString(id);
                accountService.deleteAccount(accountId);
                return ResponseEntity.noContent().build();
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato de ID de conta inválido para exclusão: " + id + ". Deve ser um UUID.", e);
            } catch (EntityNotFoundException e) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
            }
        }

        //Update an account
        @PutMapping("/updateAccount/{id}")
        public ResponseEntity<AccountDTO> updateAccount(@PathVariable String id, @RequestBody @Valid AccountDTO accountDTO) {
            try { // Bloco try-catch adicionado para as exceções do serviço
                UUID accountId = UUID.fromString(id);
            
                Account updatedAccount = accountService.updateAccount(accountId, accountDTO);
                AccountDTO updatedAccountDTO = accountMapper.toDTO(updatedAccount);
                
                return ResponseEntity.ok(updatedAccountDTO);
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Formato de ID de conta inválido para atualização: " + id + ". Deve ser um UUID.", e);
            } catch (EntityNotFoundException e) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
            } catch (AccountAlreadyExistsException e) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage(), e);
            }
        }
    }
 

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(newAccountDTO.getId())
                .toUri();

        return ResponseEntity.created(location).body(newAccountDto);
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
public ResponseEntity<AccountDto> updateAccount(@PathVariable String id, @RequestBody @Valid AccountDto accountDTO) {
    UUID accountId = UUID.fromString(id);
 
    Account updatedAccount = accountService.updateAccount(accountId, accountDTO);
    AccountDto updatedAccountDTO = accountMapper.toDto(updatedAccount);
    
    return ResponseEntity.ok(updatedAccountDTO);
}
}

