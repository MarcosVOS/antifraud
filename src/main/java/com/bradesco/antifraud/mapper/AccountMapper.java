package com.bradesco.antifraud.mapper;

import com.bradesco.antifraud.dto.AccountDto;
import com.bradesco.antifraud.dto.CreateAccountDTO;
import com.bradesco.antifraud.model.Account;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING, uses = {CustomerMapper.class})
public interface AccountMapper {

    //@Mapping(target = "id", ignore = true)
    @Mapping(target = "customer", ignore = true)
    Account toEntity(AccountDto accountDto);

    @Mapping(source = "customer.id", target = "customerId")
    AccountDto toDto(Account account);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true) // Não atualiza o ID da entidade
    @Mapping(target = "customer", ignore = true)
        // A atualização do Customer (objeto) é tratada no serviço
    void partialUpdate(@MappingTarget Account account, AccountDto accountDto); // Alterado para void e @MappingTarget primeiro


    @Mapping(source = "customerId", target = "customer.id")
    Account toEntity(CreateAccountDTO createAccountDTO);

    @InheritInverseConfiguration(name = "toEntity")
    CreateAccountDTO newAccounttoDto(Account account);

    @InheritConfiguration(name = "toEntity")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Account newAccountpartialUpdate(CreateAccountDTO createAccountDTO, @MappingTarget Account account);
}