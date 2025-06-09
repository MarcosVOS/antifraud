package com.bradesco.antifraud.repository;

import com.bradesco.antifraud.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    @param cpf 
    @return 
    Optional<Customer> findByCpf(String cpf);

    @param cpf 
    @return 
    boolean existsByCpf(String cpf);

    @param email 
    @return 
    Optional<Customer> findByEmail(String email);

    @param email 
    @return 
    boolean existsByEmail(String email);
}