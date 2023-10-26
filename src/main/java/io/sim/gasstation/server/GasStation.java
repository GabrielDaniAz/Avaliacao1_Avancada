package io.sim.gasstation.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import io.sim.bank.client.BankDocument;

public class GasStation implements Runnable {

    private final int PORT = 54322;  // Porta do servidor
    private final double price = 5.87;  // Preço por litro de combustível

    private BankDocument bankDocument;

    public GasStation() {
    }

    public synchronized double refuel(double quantity) {
        // Calcula o custo do reabastecimento com base na quantidade de combustível e no preço por litro.
        return quantity * price;
    }

    @Override
    public void run() {
        try {
            openAccount();  // Abre uma conta no banco para a estação de serviço
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Servidor GasStation aberto na porta " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new GasStationHandler(this, clientSocket)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openAccount(){
        // Abre uma conta no banco em nome da estação de serviço.
        this.bankDocument = new BankDocument("GasStation");
        this.bankDocument.openAccount();
    }
}
