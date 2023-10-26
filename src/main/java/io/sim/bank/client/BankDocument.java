package io.sim.bank.client;

import java.security.SecureRandom;

public class BankDocument {

    private String login; // Nome do titular da conta
    private String password; // Senha da conta

    public BankDocument(String name) {
        this.login = name;
        this.password = generatePassword(); // Gera uma senha aleatória ao criar um documento bancário
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    private String generatePassword() {
        int length = 8; // Comprimento da senha
        String caracteresPermitidos = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int indice = random.nextInt(caracteresPermitidos.length());
            password.append(caracteresPermitidos.charAt(indice));
        }
        return password.toString(); // Gera uma senha aleatória e a retorna
    }

    public void openAccount() {
        startAndJoin(new Thread(new BankClient(this, login, password, "openAccount")));
    }

    public void deposit(double amount) {
        startAndJoin(new Thread(new BankClient(this, login, password, "deposit", amount)));
    }

    public void withdraw(double amount) {
        startAndJoin(new Thread(new BankClient(this, login, password, "withdraw", amount)));
    }

    public void transfer(double amount, String recipient) {
        startAndJoin(new Thread(new BankClient(this, login, password, "transfer", amount, recipient)));
    }

    private void startAndJoin(Thread t) {
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
