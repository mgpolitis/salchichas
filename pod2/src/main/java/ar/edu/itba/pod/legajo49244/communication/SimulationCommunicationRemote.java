package ar.edu.itba.pod.legajo49244.communication;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;

import ar.edu.itba.pod.simul.communication.AgentDescriptor;
import ar.edu.itba.pod.simul.communication.NodeAgentLoad;
import ar.edu.itba.pod.simul.communication.SimulationCommunication;

public class SimulationCommunicationRemote implements SimulationCommunication {

	private static final SimulationCommunication INSTANCE = new SimulationCommunicationRemote();

	private SimulationCommunicationRemote() {
		System.out.println("Creating SimulationCommunication");
		try {
			UnicastRemoteObject.exportObject(this,0);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static SimulationCommunication getInstance() {
		return INSTANCE;
	}

	@Override
	public NodeAgentLoad getMinimumNodeKnownLoad() throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<AgentDescriptor> migrateAgents(int numberOfAgents)
			throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void nodeLoadModified(NodeAgentLoad newLoad) throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void startAgent(AgentDescriptor descriptor) throws RemoteException {
		// TODO Auto-generated method stub

	}

}
