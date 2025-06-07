package com.bradesco.antifraud.dto;

import com.bradesco.antifraud.model.Account;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;
@Data
@Builder
public class AccountDTO {



    private UUID id;
    @NotNull
    private String accountNumber;
    @NotBlank(message = "Agency cannot be blank")
    private String agency;
    @NotNull(message = "Balance cannot be null")
    private BigDecimal balance;
    @NotNull(message = "Account type cannot be null")
    private Account.AccountType accountType;
    @NotNull(message = "Account status cannot be null")
    private Account.AccountStatus accountStatus;
    @NotNull(message = "Customer ID cannot be null")
    private UUID customerId;

}
