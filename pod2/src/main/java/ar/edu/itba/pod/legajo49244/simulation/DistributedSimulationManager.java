package ar.edu.itba.pod.legajo49244.simulation;

import java.util.Collection;

import ar.edu.itba.pod.legajo49244.dispatcher.DispatcherListener;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.simulation.Agent;
import ar.edu.itba.pod.simul.simulation.Simulation;
import ar.edu.itba.pod.simul.simulation.SimulationInspector;
import ar.edu.itba.pod.simul.simulation.SimulationManager;

public class DistributedSimulationManager implements SimulationManager, DispatcherListener {

	@Override
	public void addAgent(Agent agent) {
		// TODO Auto-generated method stub

	}

	@Override
	public Collection<Agent> getAgents() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SimulationInspector inspector() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> void register(Class<T> type, T instance) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeAgent(Agent agent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub

	}

	@Override
	public Simulation simulation() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	@Override
	public boolean onDisconnect(Message message) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onNodeAgentsLoad(Message message) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onNodeAgentsLoadRequest(Message message) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onNodeMarketData(Message message) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onNodeMarketDataRequest(Message message) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onResourceRequest(Message message) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onResourceTransfer(Message message) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onResourceTransferCanceled(Message message) {
		// TODO Auto-generated method stub
		return false;
	}

}
