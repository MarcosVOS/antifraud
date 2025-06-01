package com.bradesco.antifraud.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;

@Entity
public class Transaction {
    
    @Id
    private UUID id;
    
    public enum TransactionType{
        DEPOSITO,
        SAQUE,
        TRANSFERENCIA, 
        PAGAMENTO
    }

    @NotNull
    private TransactionType tipo;
    @NotNull
    private BigDecimal valor;
    @NotNull
    private LocalDateTime dataHora;
    private String descricao;
    
    @ManyToOne
    private Account contaDeOrigem;
    @ManyToOne
    private Account contaDeDestino;
    

    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }

    public TransactionType getTipo(){
        return tipo;
    }
    public void setTipo(TransactionType tipo){
        this.tipo = tipo;
    }

    public BigDecimal getValor() {
        return valor;
    }
    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }
    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public String getDescricao() {
        return descricao;
    }
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
    
    public Account getContaDeOrigem() {
        return contaDeOrigem;
    }
    public void setContaDeOrigem(Account contaDeOrigem) {
        this.contaDeOrigem = contaDeOrigem;
    }
    public Account getContaDeDestino() {
        return contaDeDestino;
    }
    public void setContaDeDestino(Account contaDeDestino) {
        this.contaDeDestino = contaDeDestino;
    }

}

