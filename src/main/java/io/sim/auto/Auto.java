package io.sim.auto;

import de.tudresden.sumo.cmd.Vehicle;
import java.util.ArrayList;

import it.polito.appeal.traci.SumoTraciConnection;
import de.tudresden.sumo.objects.SumoColor;
import de.tudresden.sumo.objects.SumoPosition2D;

public class Auto extends Thread {

    // Atributos do veículo
    private String idAuto;
    private SumoColor colorAuto;
    private String driverID;
    private SumoTraciConnection sumo;

    private boolean on_off;
    private long acquisitionRate;
    private int fuelType; // 1-diesel, 2-gasoline, 3-ethanol, 4-hybrid
    private double fuelPrice; // preço em litros
    private int personCapacity; // o número total de pessoas que podem andar neste veículo
    private int personNumber; // o número total de pessoas que estão andando neste veículo

    private ArrayList<DrivingData> drivingReport;

    private final double totalFuelTank = 10;
    private double currentTank;
    private boolean isRunning;

    private double currentOdometer;
    private double lastOdometer;

    public Auto(String _idAuto, SumoColor _colorAuto, SumoTraciConnection _sumo, int _fuelType) throws Exception {
        // Inicializa os atributos do veículo
        this.idAuto = _idAuto;
        this.colorAuto = _colorAuto;
        this.sumo = _sumo;
        this.acquisitionRate = 1000;
        this.on_off = false;
        this.currentTank = totalFuelTank;
        this.fuelType = (_fuelType >= 1 && _fuelType <= 4) ? _fuelType : 4;
        this.drivingReport = new ArrayList<>();
        this.currentOdometer = 0;
        this.lastOdometer = 0;
    }

	@Override
    public void run() {
        this.on_off = true;
        while (this.on_off) {
            try {
                Auto.sleep(this.acquisitionRate);
                this.atualizaSensores();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void atualizaSensores() {
        
			try {
				// Verifique se a conexão com o SUMO está aberta
				if (!this.getSumo().isClosed()) {
					int currentSpeed = (currentTank > 3) ? 10 : 0;

					SumoPosition2D sumoPosition2D = (SumoPosition2D) sumo.do_job_get(Vehicle.getPosition(this.idAuto));
					// Cria um relatório de condução com dados atualizados
					DrivingData _report;
					_report = new DrivingData(
				        this.idAuto, this.driverID, System.currentTimeMillis(), sumoPosition2D.x, sumoPosition2D.y,
				        (String) this.sumo.do_job_get(Vehicle.getRoadID(this.idAuto)),
				        (String) this.sumo.do_job_get(Vehicle.getRouteID(this.idAuto)),
				        (double) sumo.do_job_get(Vehicle.getSpeed(this.idAuto)),
				        (double) sumo.do_job_get(Vehicle.getDistance(this.idAuto)),
				        (double) sumo.do_job_get(Vehicle.getFuelConsumption(this.idAuto)),
				        1,
				        this.fuelType, this.fuelPrice,
				        (double) sumo.do_job_get(Vehicle.getCO2Emission(this.idAuto)),
				        (double) sumo.do_job_get(Vehicle.getHCEmission(this.idAuto)),
				        this.personCapacity,
				        this.personNumber);

						this.drivingReport.add(_report);

					sumo.do_job_set(Vehicle.setSpeedMode(this.idAuto, 0));
					sumo.do_job_set(Vehicle.setSpeed(this.idAuto, currentSpeed));

					currentOdometer = _report.getOdometer();
					double percorredDistance = currentOdometer - lastOdometer;

					consumeFuel(percorredDistance);

					String info = "AutoID: " + this.getIdAuto() + " -> " +
								"odometer = " + String.format("%.2f", currentOdometer) + " -> " +
								"Tank = " + String.format("%.2f", currentTank) +
								"\n****************************************************";

					System.out.println(info);

					lastOdometer = currentOdometer;
					} else {
						System.out.println("SUMO is closed...");
					}
			} catch (Exception e) {
				e.printStackTrace();
			}

            
    }

    private void consumeFuel(double percorredDistance) {
        // Calcula o consumo de combustível com base na distância percorrida
        currentTank -= percorredDistance / 100;
    }

    // Métodos getters
    public long getAcquisitionRate() {
        return this.acquisitionRate;
    }

    public String getIdAuto() {
        return this.idAuto;
    }

    public SumoTraciConnection getSumo() {
        return this.sumo;
    }

    public int getFuelType() {
        return this.fuelType;
    }

    public SumoColor getColorAuto() {
        return this.colorAuto;
    }

    public boolean isOn() {
        return on_off;
    }

    public void setOn_off(boolean on_off) {
        this.on_off = on_off;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public boolean isLowFuel(){
        return (currentTank < 3);
    }

    public void refuelAuto(){
        currentTank = totalFuelTank;
    }

    public double getQuantityToFuel(){
        return totalFuelTank - currentTank;
    }
}
