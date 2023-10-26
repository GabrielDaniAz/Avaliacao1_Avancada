package io.sim.bank.server;

import java.util.ArrayList;

public class Account {

    private String login; // Nome do titular da conta
    private String password; // Senha da conta
    private double balance; // Saldo da conta

    private ArrayList<BankTransaction> historicalTransactions; // Lista de transações históricas na conta

    Account(String login, String password, double balance) {
        this.login = login;
        this.password = password;
        this.balance = balance;

        this.historicalTransactions = new ArrayList<>();
    }

    String getLogin() {
        return login;
    }

    String getPassword() {
        return password;
    }

    double getBalance() {
        return balance;
    }

    synchronized boolean deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            historicalTransactions.add(new BankTransaction(login, "deposit", true, amount, ""));
            return true;
        }

        historicalTransactions.add(new BankTransaction(login, "deposit", false, amount, ""));
        return false;
    }

    synchronized boolean withdraw(double amount) {
        if ((amount > balance) || (amount <= 0)) {
            historicalTransactions.add(new BankTransaction(login, "withdraw", false, amount, ""));
            return false;
        }

        balance -= amount;
        historicalTransactions.add(new BankTransaction(login, "withdraw", true, amount, ""));
        return true;
    }

    synchronized boolean transfer(Account recipient, double amount) {
        if ((amount > balance || amount <= 0) || recipient == null || recipient == this) {
            historicalTransactions.add(new BankTransaction(login, "transfer", false, amount, recipient.getLogin()));
            return false;
        }

        balance -= amount;
        recipient.deposit(amount);

        historicalTransactions.add(new BankTransaction(login, "transfer", true, amount, recipient.getLogin()));
        return true;
    }
}
