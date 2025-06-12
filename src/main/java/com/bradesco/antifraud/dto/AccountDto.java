package com.bradesco.antifraud.dto;

import com.bradesco.antifraud.model.Account;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for {@link com.bradesco.antifraud.model.Account}
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountDto implements Serializable {

    @JsonProperty("id")
    private UUID id;

    @JsonProperty("account_number")
    String accountNumber;


    @JsonProperty("agency")
    String agency;


    @JsonProperty("balance")
    BigDecimal balance;

    @JsonProperty("account_type")
    Account.AccountType accountType;

    @JsonProperty("account_status")
    Account.AccountStatus accountStatus;

    @JsonProperty("customerId")
    UUID customerId; // Assuming this is the ID of the associated customer
}