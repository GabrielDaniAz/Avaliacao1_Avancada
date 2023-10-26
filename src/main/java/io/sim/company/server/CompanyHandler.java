package io.sim.company.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import org.json.JSONObject;

public class CompanyHandler implements Runnable {

    private Company company;
    private Socket clientSocket;

    public CompanyHandler(Company company, Socket clientSocket) {
        this.company = company;
        this.clientSocket = clientSocket;
    }

    private JSONObject readClientJson() {
        try {
            // Criação de um BufferedReader para ler a entrada do cliente.
            BufferedReader bfr = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // Lê o JSON enviado pelo cliente.
            return new JSONObject(bfr.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private JSONObject performCompanyAction(JSONObject clientJson) {

        if (clientJson == null) {
            return createErrorResponse("client JSON nulo.");
        }

        String action = clientJson.getString("action");

        if (action.equals("getRoute")) {
            String[] route = company.getRandomRoute();
            if (route != null) {
                JSONObject response = new JSONObject();
                response.put("idRoute", route[0]);
                response.put("route", route[1]);
                return response;
            } else {
                // Trate o cenário em que nenhuma rota está disponível.
                return createErrorResponse("Não foi possível pegar uma rota.");
            }
        } else {
            // Trate o cenário em que o campo "action" está ausente.
            return createErrorResponse("Missing 'action' field in the request.");
        }
    }

    private void sendResponse(JSONObject result) {

        try {
            PrintWriter pWriter = new PrintWriter(clientSocket.getOutputStream(), true);

            // Envia a resposta JSON de volta ao cliente.
            pWriter.println(result.toString());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        try {
            JSONObject action = readClientJson();
            JSONObject result = performCompanyAction(action);
            sendResponse(result);
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JSONObject createErrorResponse(String message) {
        // Cria um JSON de resposta de erro com a mensagem fornecida.
        JSONObject errorResponse = new JSONObject();
        errorResponse.put("error", message);
        return errorResponse;
    }
}
