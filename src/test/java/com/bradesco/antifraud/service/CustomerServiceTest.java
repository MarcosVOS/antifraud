package com.bradesco.antifraud.service;

import com.bradesco.antifraud.model.Customer;
import com.bradesco.antifraud.repository.CustomerRepository;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CustomerServiceTest {

    CustomerRepository repository = Mockito.mock(CustomerRepository.class);
    PasswordEncoder passwordEncoder = Mockito.mock(PasswordEncoder.class);
    CustomerService service = new CustomerService(repository, passwordEncoder);

    @Test
    void testFindById() {
        UUID id = UUID.randomUUID();
        Customer customer = new Customer();
        customer.setId(id);
        customer.setName("Test User");

        when(repository.findById(id)).thenReturn(Optional.of(customer));

        Customer result = service.findById(id);

        assertNotNull(result);
        assertEquals("Test User", result.getName());
    }
}
