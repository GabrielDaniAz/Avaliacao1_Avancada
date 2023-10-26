package io.sim.bank.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.json.JSONObject;

public class BankClient implements Runnable {

    private final int PORT = 55443;
    private JSONObject clientJson;
    
    private BankDocument document;
    private String action;

    public BankClient(BankDocument document, String login, String password, String action) {
        // Inicializa o cliente do banco com informações básicas e a ação a ser executada
        this.clientJson = new JSONObject();
        this.document = document;
        this.action = action;

        // Adiciona informações ao objeto JSON para envio
        clientJson.put("login", login);
        clientJson.put("password", password);
        clientJson.put("action", action);
    }

    public BankClient(BankDocument document, String login, String password, String action, double amount) {
        // Construtor sobrecarregado para ação de transferência com valor
        this(document, login, password, action);

        // Adiciona o valor da transferência ao objeto JSON
        clientJson.put("amount", amount);
    }

    public BankClient(BankDocument document, String login, String password, String action, double amount, String recipient) {
        // Construtor sobrecarregado para ação de transferência com valor e destinatário
        this(document, login, password, action, amount);

        // Adiciona o destinatário ao objeto JSON
        clientJson.put("recipient", recipient);
    }

    private void sendJson(Socket clientSocket) {
        try {
            // Envia o objeto JSON para o servidor bancário
            PrintWriter pWriter = new PrintWriter(clientSocket.getOutputStream(), true);
            pWriter.println(clientJson.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readResponse(Socket clientSocket) {
        try {
            // Lê a resposta do servidor e a converte para um objeto JSON
            BufferedReader bfr = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String responseString = bfr.readLine();

            JSONObject responseJson = new JSONObject(responseString);

            // Exibe informações da resposta
            printInfo(responseJson);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printInfo(JSONObject json) {
        // Exibe informações da resposta, como sucesso ou falha
        boolean success = json.getBoolean("success");
        System.out.println(document.getLogin() + " - > " + action + ": obteve " + success);
    }

    @Override
    public void run() {
        try {
            // Estabelece uma conexão com o servidor bancário e realiza as operações
            Socket clientSocket = new Socket("localhost", PORT);
            sendJson(clientSocket);
            readResponse(clientSocket);
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
