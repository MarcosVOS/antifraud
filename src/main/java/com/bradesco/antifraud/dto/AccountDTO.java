package com.bradesco.antifraud.dto;

import com.bradesco.antifraud.model.Account;
import com.bradesco.antifraud.model.Customer;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToOne;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
public class AccountDTO {

    private UUID id;
    private String accountNumber;
    private String agency;
    private BigDecimal balance;
    private Account.AccountType accountType;
    private Account.AccountStatus accountStatus;
    private Customer customer;

}
