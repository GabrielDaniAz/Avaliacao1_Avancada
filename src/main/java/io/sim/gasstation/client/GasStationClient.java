package io.sim.gasstation.client;

import org.json.JSONObject;
import io.sim.auto.Driver;
import java.io.*;
import java.net.Socket;

public class GasStationClient implements Runnable {

    private Driver driver;
    private final int PORT = 54322;
    JSONObject requestJson;

    public GasStationClient(Driver driver, double quantity) {
        this.driver = driver;
        this.requestJson = new JSONObject();
        requestJson.put("action", "refuel");
        requestJson.put("quantity", quantity);
    }

    @Override
    public void run() {

        try {
            Socket socket = new Socket("localhost", PORT);
            sendRequest(socket, requestJson);
            JSONObject response = readResponse(socket);
            handleResponse(response);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendRequest(Socket socket, JSONObject request) throws IOException {
        PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        out.println(request.toString());
    }

    private JSONObject readResponse(Socket socket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String responseString = in.readLine();
        return new JSONObject(responseString);
    }

    private void handleResponse(JSONObject response) {
        if (response.has("error")) {
            // Se houver um campo "error" na resposta, ocorreu um erro durante o reabastecimento.
            System.out.println("Erro ao reabastecer: " + response.getString("error"));
        } else {
            // Caso contrário, o reabastecimento foi bem-sucedido, e a resposta contém o preço.
            double price = response.getDouble("price");
            driver.setPrice(price);
        }
    }
}
