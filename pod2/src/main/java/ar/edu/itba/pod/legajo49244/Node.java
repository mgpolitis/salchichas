package ar.edu.itba.pod.legajo49244;

import java.net.Inet4Address;
import java.net.UnknownHostException;

import ar.edu.itba.pod.legajo49244.communication.ConnectionManagerRemote;
import ar.edu.itba.pod.legajo49244.dispatcher.MessageDispatcher;
import ar.edu.itba.pod.legajo49244.dispatcher.SimulationListener;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.communication.MessageListener;

public class Node {

	private ConnectionManager connectionManager;
	private MessageListener messageListener;
	public final static String NODE_ID = getNodeId(); 
	
	public Node() {
		connectionManager = ConnectionManagerRemote.getInstance();
		SimulationListener simulationEventsHandler = new SimulationEventsHandler();
		this.messageListener = new MessageDispatcher(simulationEventsHandler);
	}

	private static String getNodeId() {
		try {
			return Inet4Address.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			throw new IllegalStateException("Couldn't determine hosts ip address.");
		}
	}

	public static void main(String[] args) {
		new Node();

	}

}
