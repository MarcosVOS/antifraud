package com.bradesco.antifraud.model;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Account {

    @Id
    private UUID id;
    //Aguardando versão completa da entidade Account.
}
