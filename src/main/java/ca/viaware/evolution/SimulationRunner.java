package ca.viaware.evolution;

import java.util.concurrent.Callable;

public class SimulationRunner implements Callable<Object> {

    private Simulator simulator;

    public SimulationRunner(Simulator simulator) {
        this.simulator = simulator;
    }

    public Object call() {
        for (int i = 0; i < 900; i++) {
            simulator.run();
        }
        simulator.setAverages();
        simulator.setFitness();

        return null;
    }
}
