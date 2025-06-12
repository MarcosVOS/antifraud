package com.bradesco.antifraud.dto;

import com.bradesco.antifraud.model.Transaction;
import org.mapstruct.*;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface TransactionMapper {
    @Mapping(source = "contaDeDestinoId", target = "contaDeDestino.id")
    @Mapping(source = "contaDeOrigemId", target = "contaDeOrigem.id")
    Transaction toEntity(TransactionDto transactionDto);

    @InheritInverseConfiguration(name = "toEntity")
    TransactionDto toDto(Transaction transaction);

    @InheritConfiguration(name = "toEntity")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Transaction partialUpdate(TransactionDto transactionDto, @MappingTarget Transaction transaction);
}