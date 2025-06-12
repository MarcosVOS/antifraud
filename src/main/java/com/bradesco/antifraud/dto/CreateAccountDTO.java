package com.bradesco.antifraud.dto;

import com.bradesco.antifraud.model.Account;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

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
public class CreateAccountDTO implements Serializable {

    @JsonIgnore
    private UUID id;

    @JsonProperty("account_number")
    String accountNumber;

    @NotNull
    @JsonProperty("agency")
    @NotNull(message = "Agency cannot be null")
    String agency;

    @NotNull
    @JsonProperty("balance")
    BigDecimal balance;

    @JsonProperty("account_type")
    Account.AccountType accountType;

    @JsonProperty("account_status")
    Account.AccountStatus accountStatus;

    @JsonProperty("customerId")
    UUID customerId;
}