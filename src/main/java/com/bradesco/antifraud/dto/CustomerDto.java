package com.bradesco.antifraud.dto;

import jakarta.validation.constraints.*;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for {@link com.bradesco.antifraud.model.Customer}
 */
@Value
public class CustomerDto implements Serializable {
    UUID id;
    @NotBlank(message = "Name is required")
    String name;
    @NotNull(message = "CPF is required")
    String cpf;
    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    LocalDate dateOfBirth;
    @NotNull(message = "Email is required")
    @Email(message = "Invalid email")
    String email;
    @Pattern(message = "Invalid phone number", regexp = "\\+?\\d{10,15}")
    String phone;
    AddressDto address;
    @NotBlank(message = "Password is required")
    String password;

    /**
     * DTO for {@link com.bradesco.antifraud.model.Address}
     */
    @Value
    public static class AddressDto implements Serializable {
        @NotBlank(message = "Street is required")
        String street;
        @NotBlank(message = "Number is required")
        String number;
        @NotBlank(message = "Neighborhood is required")
        String neighborhood;
        @NotBlank(message = "City is required")
        String city;
        @Size(min = 2, max = 2)
        @NotBlank(message = "State is required")
        String state;
        @Pattern(message = "Invalid zip code format", regexp = "\\d{5}-?\\d{3}")
        @NotBlank(message = "ZipCode is required")
        String zipCode;
    }
}