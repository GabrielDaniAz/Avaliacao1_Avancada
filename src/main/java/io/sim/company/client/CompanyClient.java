package io.sim.company.client;

import org.json.JSONObject;

import io.sim.auto.Driver;
import io.sim.transport.Itinerary;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class CompanyClient implements Runnable {
    private final int PORT = 54321;
    private Driver driver;
    private JSONObject clientJson;

    public CompanyClient(Driver driver) {
        this.driver = driver;
        this.clientJson = new JSONObject();

        // Define a ação a ser realizada pelo cliente
        clientJson.put("action", "getRoute");
    }

    private void sendJson(Socket clientSocket) {
        try {
            // Envia o JSON para o servidor da empresa
            PrintWriter pWriter = new PrintWriter(clientSocket.getOutputStream(), true);
            pWriter.println(clientJson.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readResponse(Socket clientSocket) {
        try {
            // Lê a resposta do servidor da empresa
            BufferedReader bfr = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String response = bfr.readLine();

            // Converte a resposta em um objeto JSON
            JSONObject responseJson = new JSONObject(response);

            // Cria um itinerário com base na resposta
            createItinerary(responseJson);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createItinerary(JSONObject json) {
        // Extrai informações do JSON para criar um itinerário
        String route = json.getString("route");
        String idRoute = json.getString("idRoute");

        String[] itineraryString = new String[]{idRoute, route};

        // Define o itinerário para o motorista
        driver.setItinerary(new Itinerary(itineraryString));
    }

    @Override
    public void run() {
        try {
            // Conecta-se ao servidor da empresa
            Socket clientSocket = new Socket("localhost", PORT);

            // Envia o JSON e lê a resposta
            sendJson(clientSocket);
            readResponse(clientSocket);

            // Fecha a conexão
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
