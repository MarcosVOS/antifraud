package com.bradesco.antifraud.mapper;

import com.bradesco.antifraud.dto.AccountDTO;
import com.bradesco.antifraud.model.Account;
import com.bradesco.antifraud.model.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(target = "customerId", source = "customer.id")
    AccountDTO toDTO(Account account);

    @Mapping(target = "customer", source = "customerId", qualifiedByName = "customerIdToCustomer")
    Account toEntity(AccountDTO accountDTO);

    @Named("customerIdToCustomer")
    default Customer customerIdToCustomer(UUID customerId) {
        if (customerId == null) {
            return null;
        }
        Customer customer = new Customer();
        customer.setId(customerId);
        return customer;
    }
}
