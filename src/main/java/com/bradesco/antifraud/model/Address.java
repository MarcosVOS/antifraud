package com.bradesco.antifraud.model;

import jakarta.persistence.Embeddable; 
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable 
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Address {

    @NotBlank
    private String street;

    @NotBlank
    private String number;

    private String complement; 

    @NotBlank
    private String neighborhood;

    @NotBlank
    private String city;

    @NotBlank
    private String state; 

    @NotBlank
    private String zipCode; 
}
