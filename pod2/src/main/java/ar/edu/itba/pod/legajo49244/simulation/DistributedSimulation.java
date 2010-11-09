package ar.edu.itba.pod.legajo49244.simulation;

import ar.edu.itba.pod.simul.local.LocalSimulation;
import ar.edu.itba.pod.simul.simulation.Simulation;
import ar.edu.itba.pod.simul.simulation.SimulationInspector;
import ar.edu.itba.pod.simul.time.TimeMapper;

public class DistributedSimulation extends LocalSimulation implements Simulation, SimulationInspector {

	public DistributedSimulation(TimeMapper timeMapper) {
		super(timeMapper);
	}

}
