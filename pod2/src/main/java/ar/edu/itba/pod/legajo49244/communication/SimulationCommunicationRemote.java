package ar.edu.itba.pod.legajo49244.communication;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;

import ar.edu.itba.pod.legajo49244.Node;
import ar.edu.itba.pod.legajo49244.communication.payload.NodeAgentLoadRequestPayloadWalter;
import ar.edu.itba.pod.legajo49244.message.Messages;
import ar.edu.itba.pod.simul.communication.AgentDescriptor;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.communication.NodeAgentLoad;
import ar.edu.itba.pod.simul.communication.SimulationCommunication;

import com.google.common.collect.Sets;
import com.google.inject.internal.Maps;

public class SimulationCommunicationRemote implements SimulationCommunication {

	private static final SimulationCommunicationRemote INSTANCE = new SimulationCommunicationRemote();

	private boolean isCoordinator = false;
	private String coodinatorId = null;

	private SortedSet<NodeAgentLoad> sortedLoadPerNode;
	private Map<String, NodeAgentLoad> mapLoadPerNode;

	private SimulationCommunicationRemote() {
		System.out.println("Creating SimulationCommunication");
		try {
			UnicastRemoteObject.exportObject(this, 0);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static SimulationCommunicationRemote get() {
		return INSTANCE;
	}

	@Override
	public NodeAgentLoad getMinimumNodeKnownLoad() throws RemoteException {
		// TODO: complete and make thread safe
		if (!isCoordinator) {
			return null;
		}
		return sortedLoadPerNode.first();
	}

	@Override
	public Collection<AgentDescriptor> migrateAgents(int numberOfAgents)
			throws RemoteException {
		// TODO get agents from local simulation and release them!
		return null;
	}

	@Override
	public void nodeLoadModified(NodeAgentLoad newLoad) throws RemoteException {
		
		// TODO: REFACTOR MORTAL, no me lo llama otro...
		if (!isCoordinator) {
			// the caller node should now be coordinator
			throw new IllegalStateException(
					"I am not coordinator, go bug other");
		}
		String node = newLoad.getNodeId();
		NodeAgentLoad oldLoad = mapLoadPerNode.get(node);
		if (oldLoad != null) {
			sortedLoadPerNode.remove(oldLoad);
			sortedLoadPerNode.add(newLoad);
		} else {
			sortedLoadPerNode.add(newLoad);
		}
		mapLoadPerNode.put(node, newLoad);
		System.out.println("this should be always true: "
				+ (mapLoadPerNode.keySet().size() == sortedLoadPerNode.size()));
	}

	@Override
	public void startAgent(AgentDescriptor descriptor) throws RemoteException {
		// TODO aca ya seguro hay que levantar el agent, alguien
		// se ocupo de controlar que sea lo que se debe.

		// TODO: no le aviso al coordinador que cambio mi carga, no es necesario?
//		NodeAgentLoad newLoad = null;
//		try {
//			getCoordinatorConnectionManager().getSimulationCommunication()
//					.nodeLoadModified(newLoad);
//		} catch (RemoteException e) {
//			// TODO: coordinator down, check if correct
//			this.becomeCoordinator();
//		}

	}

	private void becomeCoordinator() {
		if (isCoordinator) {
			throw new IllegalStateException("Node is coodinator already.");
		}

		isCoordinator = true;
		sortedLoadPerNode = Sets.newTreeSet(new Comparator<NodeAgentLoad>() {

			@Override
			public int compare(NodeAgentLoad o1, NodeAgentLoad o2) {
				return new Integer(o1.getNumberOfAgents())
						.compareTo(new Integer(o2.getNumberOfAgents()));
			}
		});
		mapLoadPerNode = Maps.newHashMap();

		// TODO: analyze fully

		try {
			ClusterCommunicationRemote
					.get()
					.broadcast(
							Messages
									.newNodeAgentLoadRequestMessage(new NodeAgentLoadRequestPayloadWalter()));
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public synchronized void leaveCoordination() {
		isCoordinator = false;
		mapLoadPerNode = null;
		sortedLoadPerNode = null;
	}

	private ConnectionManager getCoordinatorConnectionManager()
			throws RemoteException {
		return ConnectionManagerRemote.get().getConnectionManager(
				coodinatorId);
	}

	public void onNodeDisconnected(String disconnectedNode) {
		// TODO: ver los sysos estos, son informales
		if (Node.getNodeId().equals(disconnectedNode)) {
			try {
				if (ClusterAdministrationRemote.get()
						.isConnectedToGroup()) {
					System.out
							.println("Me dijeron que yo me desconecte y estoy conectado! WTF? ");
				}
			} catch (RemoteException e) {
				System.out.println("ESTO NO DEBERIA APARECER NUNCA");
				// do nothing, will never fail
			}
		}
		if (isCoordinator) {
			NodeAgentLoad disconnectedAgentLoad = mapLoadPerNode.get(disconnectedNode);
			if (disconnectedAgentLoad == null) {
				System.out.println("I was notified of a node's disconection,"
						+ "but I didn't know of it's existance= "
						+ disconnectedNode);

			} else {
				sortedLoadPerNode.remove(disconnectedAgentLoad);
			}
			mapLoadPerNode.remove(disconnectedNode);
		}
	}

	public void onNodeAgentsLoadInfoArrived(String informerNode, int load) {
		if (isCoordinator) {
			//TODO: update node info and update sleep interval
			
			
		}
	}
	
	public void nodeBalancingInfoCallback(Long dt) {
		// TODO: do
	}

}
