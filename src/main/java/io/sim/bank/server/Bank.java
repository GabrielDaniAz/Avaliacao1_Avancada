package io.sim.bank.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class Bank implements Runnable {

    private final int PORT = 55443; // Porta do servidor bancário
    private boolean isOpen; // Indica se o banco está aberto ou fechado

    private final double INITIAL_BALANCE = 500; // Saldo inicial para novas contas bancárias

    private ConcurrentHashMap<String, Account> accounts; // Armazena as contas bancárias
    private ArrayList<BankTransaction> historicalTransactions; // Lista de transações históricas no banco

    private String name; // Nome do banco

    public Bank(String name) {
        this.name = name;
        this.isOpen = true;
        this.accounts = new ConcurrentHashMap<>();
        this.historicalTransactions = new ArrayList<>();
    }

    public int getServidorPort() {
        return PORT;
    }

    synchronized boolean openAccount(String login, String password) {
        login = login.replace(" ", "");

        while (accounts.containsKey(login)) {
            login += "_";
        }

        Account account = new Account(login, password, INITIAL_BALANCE);
        accounts.put(login, account);

        historicalTransactions.add(new BankTransaction(login, "openAccount", true, 0, ""));
        return true;
    }

    Account getAccount(String login, String password) {
        Account account = accounts.get(login);

        if (account == null || !account.getPassword().equals(password)) {
            return null;
        }
        return account;
    }

    Account verifyRecipientAccount(String login) {
        return accounts.get(login);
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Servidor " + name + " está aberto na porta " + PORT);

            while (isOpen) {
                Socket clientSocket = serverSocket.accept();

                new Thread(new BankService(clientSocket, this)).start();
            }
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
