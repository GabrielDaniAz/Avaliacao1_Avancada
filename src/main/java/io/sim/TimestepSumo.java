package io.sim;

import it.polito.appeal.traci.SumoTraciConnection;

public class TimestepSumo extends Thread {
    private SumoTraciConnection sumo;
    private long acquisitionRate;

    public TimestepSumo(SumoTraciConnection _sumo, long _acquisitionRate) {
        this.sumo = _sumo;
        this.acquisitionRate = _acquisitionRate;
    }

    @Override
    public void run() {
        while (true) {
            try {
                this.sumo.do_timestep();
                Thread.sleep(acquisitionRate);
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
