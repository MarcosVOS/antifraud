package com.bradesco.antifraud.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.*;

import lombok.*;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @NotBlank(message = "Street is required")
    private String street;

    @NotBlank(message = "Number is required")
    private String number;

    @NotBlank(message = "Neighborhood is required")
    private String neighborhood;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    @Size(min=2, max=2)
    private String state;

    @NotBlank(message = "ZipCode is required")
    @Pattern(regexp = "\\d{5}-?\\d{3}", message = "Invalid zip code format")
    private String zipCode;
}