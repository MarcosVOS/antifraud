package com.bradesco.antifraud.service;

import com.bradesco.antifraud.exception.accountExceptions.AccountAlreadyExistsException;
import com.bradesco.antifraud.model.Customer;
import com.bradesco.antifraud.repository.CustomerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository repository;
    private final PasswordEncoder passwordEncoder;

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

        customer.setPassword(passwordEncoder.encode(customer.getPassword()));

        return repository.save(customer);
    }

    public Customer update(UUID id, Customer newData) {
        Customer existing = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));

        if (!existing.getCpf().equals(newData.getCpf()) && repository.existsByCpf(newData.getCpf())) {
            throw new AccountAlreadyExistsException("CPF já cadastrado");
        }

        if (!existing.getEmail().equals(newData.getEmail()) && repository.existsByEmail(newData.getEmail())) {
            throw new AccountAlreadyExistsException("Email já cadastrado");
        }

        existing.setName(newData.getName());
        existing.setCpf(newData.getCpf());
        existing.setDateOfBirth(newData.getDateOfBirth());
        existing.setEmail(newData.getEmail());
        existing.setPhone(newData.getPhone());
        existing.setAddress(newData.getAddress());

        if (!newData.getPassword().equals(existing.getPassword())) {
            existing.setPassword(passwordEncoder.encode(newData.getPassword()));
        }

        return repository.save(existing);
    }

    public void delete(UUID id) {
        Customer customer = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));

        repository.deleteById(customer.getId());
    }

    public List<Customer> getAllCustomers() {
        return repository.findAll();
    }

    public Customer findByEmail(String email) {
        return repository.findByEmail(email).orElse(null);
    }
}
