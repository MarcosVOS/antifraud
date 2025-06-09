package com.bradesco.antifraud.mapper;

import com.bradesco.antifraud.dto.TransactionDTO;
import com.bradesco.antifraud.model.Account;
import com.bradesco.antifraud.model.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(source = "contaDeOrigem", target = "contaDeOrigem", qualifiedByName = "mapAccountToAccountId")
    @Mapping(source = "contaDeDestino", target = "contaDeDestino", qualifiedByName = "mapAccountToAccountId")
    TransactionDTO toDTO(Transaction transaction);

    @Mapping(source = "contaDeOrigem", target = "contaDeOrigem", qualifiedByName = "mapAccountIdToAccount")
    @Mapping(source = "contaDeDestino", target = "contaDeDestino", qualifiedByName = "mapAccountIdToAccount")
    Transaction toEntity(TransactionDTO transactionDTO);

    @Named("mapAccountToAccountId")
    default UUID mapAccountToAccountId(Account account) {
        return account != null ? account.getId() : null;
    }

    @Named("mapAccountIdToAccount")
    default Account mapAccountIdToAccount(UUID accountId) {
        if (accountId == null) {
            return null;
        }
        
        Account account = new Account();
        account.setId(accountId);
        return account;
    }
}