package com.bradesco.antifraud.repository;

import com.bradesco.antifraud.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    boolean existsByCpf(String cpf);
    boolean existsByEmail(String email);
    Optional<Customer> findByEmail(String email);
}