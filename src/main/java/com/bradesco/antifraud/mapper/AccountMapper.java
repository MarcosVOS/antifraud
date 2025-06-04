package com.bradesco.antifraud.mapper;


import com.bradesco.antifraud.dto.AccountDTO;
import com.bradesco.antifraud.model.Account;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;


// AccountMapper.java
@Mapper(componentModel = "spring")
public interface AccountMapper {


    AccountMapper INSTANCE = Mappers.getMapper(AccountMapper.class);
    AccountDTO toDTO(Account account);
    Account toEntity(AccountDTO accountDTO);
}


