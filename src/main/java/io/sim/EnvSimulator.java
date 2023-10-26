package io.sim;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import de.tudresden.sumo.objects.SumoColor;
import io.sim.auto.Auto;
import io.sim.auto.Driver;
import io.sim.bank.server.Bank;
import io.sim.company.server.Company;
import io.sim.gasstation.server.GasStation;
import it.polito.appeal.traci.SumoTraciConnection;

public class EnvSimulator extends Thread {
    private SumoTraciConnection sumo;

    public EnvSimulator() {
        // Construtor vazio
    }

    public void run() {
        // Configuração para iniciar a simulação do SUMO
        String sumo_bin = "sumo-gui";   // Caminho para o executável do SUMO
        String config_file = "map/map.sumo.cfg"; // Arquivo de configuração da simulação

        // Inicialização da conexão com o SUMO
        this.sumo = new SumoTraciConnection(sumo_bin, config_file);
        sumo.addOption("start", "1"); // Inicia automaticamente na GUI
        sumo.addOption("quit-on-end", "1"); // Fecha automaticamente no final

        try {
            sumo.runServer(12345); // Inicia o servidor SUMO

            // Inicia o timestep do simulador
            TimestepSumo test = new TimestepSumo(sumo, 10);
            test.start();

            // Inicializa um banco em uma thread separada
            Bank alphBank = new Bank("alphaBank");
            Thread alphaBankThread = new Thread(alphBank);
            alphaBankThread.start();

            // Inicia uma empresa em uma thread separada
            Company company = new Company();
            Thread companyThread = new Thread(company);
            companyThread.start();

            // Inicializa um posto de gasolina em uma thread separada
            GasStation gasStation = new GasStation();
            Thread gasStationThread = new Thread(gasStation);
            gasStationThread.start();

            ArrayList<Thread> threads = new ArrayList<>();
            Random random = new Random();
            for (int i = 0; i < 100; i++) {
                // Gera um tipo de combustível aleatório
                int fuelType = random.nextInt(4) + 1;

                // Cria um objeto Auto
                Auto auto = new Auto(String.valueOf(i + 1), new SumoColor(0, 255, 0, 126), sumo, fuelType);

                // Cria um motorista e associa-o ao veículo
                Driver driver = new Driver(i, sumo);
                driver.setAuto(auto);

                // Inicia o motorista em uma thread separada
                Thread driverThread = new Thread(driver);
                driverThread.start();
                threads.add(driverThread);
            }

            for (Thread thread : threads) {
                thread.join();
            }

            System.out.println("FIM!"); // Mensagem de conclusão
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
