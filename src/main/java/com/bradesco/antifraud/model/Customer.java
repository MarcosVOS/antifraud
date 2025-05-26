package com.bradesco.antifraud.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NotBlank(message = "Name is required")
    private String name;

    @CPF(message = "Invalid CPF")
    @NotNull(message = "CPF is required")
    @Column(unique = true, nullable = false)
    private String cpf;

    @Past(message = "Date of birth must be in the past")
    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;

    @Email(message = "Invalid email")
    @NotNull(message = "Email is required")
    @Column(unique = true, nullable = false)
    private String email;

    @Pattern(regexp = "\\+?\\d{10,15}", message = "Invalid phone number")
    private String phone;

    @Embedded
    private Address address;

    @NotBlank(message = "Password is required")
    private String password;
}
