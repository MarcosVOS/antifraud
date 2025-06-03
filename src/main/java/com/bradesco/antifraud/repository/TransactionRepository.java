package com.bradesco.antifraud.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.bradesco.antifraud.model.Transaction;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    
}
