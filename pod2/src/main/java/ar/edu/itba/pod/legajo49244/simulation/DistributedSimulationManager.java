package ar.edu.itba.pod.legajo49244.simulation;

import java.rmi.RemoteException;
import java.util.Collection;

import ar.edu.itba.pod.legajo49244.communication.ClusterAdministrationRemote;
import ar.edu.itba.pod.legajo49244.communication.ClusterCommunicationRemote;
import ar.edu.itba.pod.legajo49244.communication.SimulationCommunicationRemote;
import ar.edu.itba.pod.legajo49244.communication.payload.Payloads;
import ar.edu.itba.pod.legajo49244.dispatcher.DispatcherListener;
import ar.edu.itba.pod.legajo49244.message.Messages;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.payload.DisconnectPayload;
import ar.edu.itba.pod.simul.communication.payload.NodeAgentLoadPayload;
import ar.edu.itba.pod.simul.simulation.Agent;
import ar.edu.itba.pod.simul.simulation.SimulationInspector;
import ar.edu.itba.pod.simul.simulation.SimulationManager;
import ar.edu.itba.pod.simul.time.TimeMapper;

import com.google.common.base.Preconditions;

public class DistributedSimulationManager implements SimulationManager,
		DispatcherListener {

	private DistributedSimulation simulation;
	private boolean started;

	public void setTimeMapper(TimeMapper timeMapper) {
		simulation = new DistributedSimulation(timeMapper);
	}

	@Override
	public void start() {
		simulation.start();
		started = true;
	}

	@Override
	public void shutdown() {
		simulation().shutdown();
		started = false;
	}

	@Override
	public void addAgent(Agent agent) {
		// TODO check where in the world to insert, and do it!
	}

	public void addAgentHere(Agent agent) {
		simulation.addAgent(agent);
	}

	@Override
	public Collection<Agent> getAgents() {
		return simulation.getAgents();
	}

	@Override
	public SimulationInspector inspector() {
		return simulation();
	}

	@Override
	public <T> void register(Class<T> type, T instance) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeAgent(Agent agent) {
		// no se llamara externamente
		this.simulation.removeAgent(agent);

	}

	@Override
	public DistributedSimulation simulation() {
		Preconditions.checkState(started, "No simulation has been started!");
		return simulation;
	}

	/**
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 */

	@Override
	public boolean onDisconnect(Message message) {
		String disconnectedNode = ((DisconnectPayload) message.getPayload())
				.getDisconnectedNodeId();
		ClusterAdministrationRemote.get().onNodeDisconnected(disconnectedNode);
		SimulationCommunicationRemote.get()
				.onNodeDisconnected(disconnectedNode);

		return true;
	}

	@Override
	public boolean onNodeAgentsLoad(Message message) {
		int load = ((NodeAgentLoadPayload) message.getPayload()).getLoad();
		String informerNode = message.getNodeId();
		SimulationCommunicationRemote.get().onNodeAgentsLoadInfoArrived(
				informerNode, load);
		return true;
	}

	@Override
	public boolean onNodeAgentsLoadRequest(Message message) {
		// sender node is coordinator, tell him your load
		String coordId = message.getNodeId();
		int load = inspector().runningAgents();
		try {
			ClusterCommunicationRemote.get().send(
					Messages.newNodeAgentLoadResponseMessage(Payloads
							.newNodeAgentLoadPayload(load)), coordId);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public boolean onNodeMarketData(Message message) {
		// TODO averiguar q carajo es esto
		return false;
	}

	@Override
	public boolean onNodeMarketDataRequest(Message message) {
		// TODO averiguar q carajo es esto
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
