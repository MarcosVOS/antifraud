package com.bradesco.antifraud.service;

import com.bradesco.antifraud.model.Customer;
import com.bradesco.antifraud.repository.CustomerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.bradesco.antifraud.exception.DuplicateResourceException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository repository;

    public Customer findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));
    }

    public Customer create(Customer customer) {
        if (repository.existsByCpf(customer.getCpf())) {
            throw new DuplicateResourceException("CPF já cadastrado");
        }
        if (repository.existsByEmail(customer.getEmail())) {
            throw new DuplicateResourceException("E-mail já cadastrado");
        }
        return repository.save(customer);
    }

}
