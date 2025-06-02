package com.bradesco.antifraud.dto;

import com.bradesco.antifraud.model.Account;
import com.bradesco.antifraud.model.Customer;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;
@Data
@Builder
public class AccountDTO {

    @NotBlank(message = "ID cannot be blank")
    private UUID id;
    @NotBlank
    private String accountNumber;
    @NotBlank(message = "Agency cannot be blank")
    private String agency;
    @NotNull(message = "Balance cannot be null")
    private BigDecimal balance;
    @NotNull(message = "Account type cannot be null")
    private Account.AccountType accountType;
    @NotNull(message = "Account status cannot be null")
    private Account.AccountStatus accountStatus;
    //@NotNull(message = "Customer cannot be null")
    private Customer customerId;

}
