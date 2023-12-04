package io.sim.auto;

import it.polito.appeal.traci.SumoTraciConnection;
import io.sim.bank.client.BankDocument;
import io.sim.company.client.CompanyClient;
import io.sim.gasstation.client.GasStationClient;
import io.sim.transport.Itinerary;
import io.sim.transport.TransportService;

public class Driver implements Runnable {

    private String id;
    private Auto auto;
    private SumoTraciConnection sumo;
    private Itinerary itinerary;
    private boolean transporting;
    private double price;
    private BankDocument bankDocument;

    public Driver(int id, SumoTraciConnection sumo) {
        // Inicializa o motorista com um ID e a conexão com o simulador SUMO
        this.id = "D" + id;
        this.sumo = sumo;
        this.transporting = false;
    }

    public String getId() {
        return id;
    }

    public void setAuto(Auto auto) {
        this.auto = auto;
    }

    public Auto getAuto() {
        return auto;
    }

    public void setItinerary(Itinerary itinerary) {
        this.itinerary = itinerary;
    }

    public Itinerary getItinerary() {
        return itinerary;
    }

    public void setPrice(double price){
        this.price = price;
    }

    @Override
    public void run() {
        openAccount();
        while (true) {
            if (!transporting) {
                connectCompanyAndGetRoute();
                System.out.println("Driver " + id + " iniciando transporte -> Rota: " + itinerary.getIdItinerary());
                initializeTransportService();
                transporting = true;
            }
        }
    }

    private void openAccount(){
        // Cria um documento bancário para o motorista e abre uma conta no banco
        this.bankDocument = new BankDocument(id);
        this.bankDocument.openAccount();
    }

    private void connectCompanyAndGetRoute() {
        try {
            // Conecta-se a uma empresa para obter informações sobre a rota
            CompanyClient companyClient = new CompanyClient(this);
            Thread t = new Thread(companyClient);
            t.start();
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void initializeTransportService() {
        // Inicializa o serviço de transporte para seguir uma rota
        TransportService transport = new TransportService(this, this.itinerary, sumo);
        transport.start();

        // Adicionando um loop para verificar o status do transporte
        while (transport.isAlive()) {
            try {
                Thread.sleep(1000);

                // Verifica o nível de combustível periodicamente e abastece se necessário
                refuelTank();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        transporting = false;
    }

    private void refuelTank() {
        try {
            if (auto.isLowFuel()) {
                // Se o nível de combustível estiver baixo, solicita abastecimento em um posto de gasolina
                Thread gasStationClient = new Thread(new GasStationClient(this, auto.getQuantityToFuel()));
                gasStationClient.start();
                gasStationClient.join();
                Thread.sleep(1500);
                auto.refuelAuto();
                bankDocument.transfer(price, "GasStation");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
