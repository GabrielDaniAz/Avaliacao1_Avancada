package io.sim.bank.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.json.JSONObject;

public class BankService implements Runnable {

    private Socket clientSocket; // O soquete do cliente
    private Bank bank; // A instância do banco

    public BankService(Socket clientSocket, Bank bank) {
        this.clientSocket = clientSocket;
        this.bank = bank;
    }

    private JSONObject readClientJson() {
        try {
            BufferedReader bfr = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            return new JSONObject(bfr.readLine()); // Lê o JSON enviado pelo cliente
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void sendResponse(JSONObject result) {
        try {
            PrintWriter pWriter = new PrintWriter(clientSocket.getOutputStream(), true);
            pWriter.println(result.toString()); // Envia a resposta JSON de volta ao cliente
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JSONObject performBankAction(JSONObject clientJson) {
        if (clientJson == null) {
            return getJsonForClient(false);
        }

        String login = clientJson.getString("login");
        String password = clientJson.getString("password");
        String action = clientJson.getString("action");

        if (action.equalsIgnoreCase("openAccount")) {
            boolean success = bank.openAccount(login, password);
            return getJsonForClient(success);
        }

        Account account = bank.getAccount(login, password);

        if (account == null) {
            return getJsonForClient(false);
        }

        double amount;
        String recipientStr = "";
        if (clientJson.has("amount")) {
            amount = clientJson.getDouble("amount");
            switch (action.toLowerCase()) {
                case "deposit":
                    return getJsonForClient(account.deposit(amount));
                case "withdraw":
                    return getJsonForClient(account.withdraw(amount));
                case "transfer":
                    if (clientJson.has("recipient")) {
                        recipientStr = clientJson.getString("recipient");
                        Account recipient = bank.verifyRecipientAccount(recipientStr);
                        return getJsonForClient(account.transfer(recipient, amount));
                    }
                    return getJsonForClient(false);
                default:
                    return getJsonForClient(false);
            }
        }

        return getJsonForClient(false);
    }

    private JSONObject getJsonForClient(boolean success) {
        JSONObject json = new JSONObject();

        json.put("success", success); // Cria um JSON de resposta com o resultado da ação

        return json;
    }

    @Override
    public void run() {
        try {
            JSONObject clientJson = readClientJson();
            JSONObject result = performBankAction(clientJson);
            sendResponse(result);
            clientSocket.close(); // Fecha a conexão com o cliente após a resposta
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
