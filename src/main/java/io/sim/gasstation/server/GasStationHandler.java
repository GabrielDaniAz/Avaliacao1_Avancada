package io.sim.gasstation.server;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class GasStationHandler implements Runnable {

    private GasStation gasStation;
    private Socket clientSocket;

    public GasStationHandler(GasStation gasStation, Socket clientSocket) {
        this.gasStation = gasStation;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            JSONObject request = readClientJson();
            JSONObject response = performGasStationAction(request);
            sendResponse(response);
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JSONObject readClientJson() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            return new JSONObject(reader.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private JSONObject performGasStationAction(JSONObject request) {
        if (request == null) {
            return createErrorResponse("Requisição JSON nula.");
        }

        String action = request.getString("action");
        double quantity = request.getDouble("quantity");

        if (action.equals("refuel")) {
            double price = gasStation.refuel(quantity);
            JSONObject response = new JSONObject();
            response.put("price", price);
            return response;
        } else {
            return createErrorResponse("Ação desconhecida na solicitação.");
        }
    }

    private void sendResponse(JSONObject response) {
        try {
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
            writer.println(response.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JSONObject createErrorResponse(String message) {
        JSONObject errorResponse = new JSONObject();
        errorResponse.put("error", message);
        return errorResponse;
    }
}
