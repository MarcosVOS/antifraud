package com.bradesco.antifraud.service;

import com.bradesco.antifraud.exception.accountExceptions.AccountAlreadyExistsException;
import com.bradesco.antifraud.model.Customer;
import com.bradesco.antifraud.repository.CustomerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
            throw new AccountAlreadyExistsException("CPF já cadastrado: " + customer.getCpf());
        }
        if (repository.existsByEmail(customer.getEmail())) {
            throw new AccountAlreadyExistsException("Email já cadastrado: " + customer.getEmail());
        }

        return repository.save(customer);
    }
}
