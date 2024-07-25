package com.microtx.sagaorchservice.dto;

import jakarta.persistence.*;

@Entity
@Table(name = "Accounts")
public class Account {
    @Column(name="ACCOUNTID")
    @Id
    String accountId;

    @Column(name="name")
    @Basic
    @OneToOne
    String name;

    @Column(name="amount")
    @Basic
    @OneToOne
    double amount;

    public Account(String accountId, String name, double amount) {
        this.accountId = accountId;
        this.name = name;
        this.amount = amount;
    }

    public Account() {

    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getName() {
        return name;
    }

    public double getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return "Account{" +
                "accountId='" + accountId + '\'' +
                ", name='" + name + '\'' +
                ", amount=" + amount +
                '}';
    }
}

