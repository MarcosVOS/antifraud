package com.bradesco.antifraud.controller;

import com.bradesco.antifraud.model.Address;
import com.bradesco.antifraud.model.Customer;
import com.bradesco.antifraud.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository repository;

    private Customer savedCustomer;

    @BeforeEach
    void setup() {
        repository.deleteAll();

        Customer customer = Customer.builder()
            .name("John Doe")
            .cpf("123.456.789-09")
            .dateOfBirth(LocalDate.of(1990, 1, 1))
            .email("john@example.com")
            .phone("+5511999999999")
            .password("123456")
            .address(Address.builder()
                .street("Rua A")
                .number("123")
                .neighborhood("Centro")
                .city("São Paulo")
                .state("SP")
                .zipCode("01001-000")
                .build())
            .build();

        savedCustomer = repository.save(customer);
    }

    @Test
    void testGetCustomerById() throws Exception {
        mockMvc.perform(get("/customers/" + savedCustomer.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("john@example.com"));
    }
}
