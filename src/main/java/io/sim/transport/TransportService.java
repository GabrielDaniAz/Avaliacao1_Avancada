package io.sim.transport;

import de.tudresden.sumo.cmd.Route;
import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.objects.SumoStringList;
import io.sim.auto.Driver;
import it.polito.appeal.traci.SumoTraciConnection;

public class TransportService extends Thread {

    private boolean on_off;
    private SumoTraciConnection sumo;
    private Driver driver;
    private Itinerary itinerary;
    private String lastRoute;
    private String vehicleId;

    public TransportService(Driver _driver, Itinerary _itinerary, SumoTraciConnection _sumo) {
        this.driver = _driver;
        this.sumo = _sumo;
        this.on_off = true;
        this.itinerary = _itinerary;
        this.vehicleId = this.driver.getAuto().getIdAuto();
    }

    @Override
    public void run() {
        System.out.println("Início da rota: " + this.driver.getAuto().getIdAuto());
        while (on_off) {
            try {
                initializeRoutes();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Atualiza o status do Auto para refletir a conclusão da rota
        this.driver.getAuto().setOn_off(false);
        System.out.println("Fim da rota: " + driver.getAuto().getIdAuto());
    }

    private void initializeRoutes() {
        SumoStringList edge = new SumoStringList();
        edge.clear();
        String route = this.itinerary.getRoute();

        for (String e : route.split(" ")) {
            edge.add(e);
        }

        this.lastRoute = edge.get(edge.size() - 1);

        try {
            sumo.do_job_set(Route.add(this.itinerary.getIdItinerary(), edge));
            
            sumo.do_job_set(Vehicle.addFull(vehicleId,
                this.itinerary.getIdItinerary(),
                "DEFAULT_VEHTYPE",
                "now",
                "0",
                "0",
                "0",
                "current",
                "max",
                "current",
                "",
                "",
                "",
                1,
                1)
            );
            
            sumo.do_job_set(Vehicle.setColor(vehicleId, this.driver.getAuto().getColorAuto()));
            sumo.do_job_set(Vehicle.setSpeed(vehicleId, 50));
            sumo.do_job_set(Vehicle.setSpeedMode(vehicleId, 31));

            // Inicie o Auto aqui
            this.driver.getAuto().start();

            while (on_off) {
                try {
                    Thread.sleep(this.driver.getAuto().getAcquisitionRate());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (((String) this.sumo.do_job_get(Vehicle.getRoadID(this.driver.getAuto().getIdAuto()))).equals(this.lastRoute)) {
                    on_off = false;
                }
            }

        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public boolean isOn() {
        return on_off;
    }
}
