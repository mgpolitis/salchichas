package ar.edu.itba.pod.legajo49244;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.rmi.RemoteException;

import ar.edu.itba.pod.legajo49244.communication.ConnectionManagerRemote;
import ar.edu.itba.pod.simul.communication.ConnectionManager;

public class Node {

	private ConnectionManager connectionManager;

	public final static String NODE_ID = getNodeId();

	public Node() {
		connectionManager = ConnectionManagerRemote.getInstance();
		try {
			// starts message listener
			connectionManager.getGroupCommunication();
			connectionManager.getClusterAdmimnistration().connectToGroup("10.6.0.82");
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String getNodeId() {
		if (NODE_ID != null) {
			return NODE_ID;
		}
		
		try {
			return Inet4Address.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			throw new IllegalStateException(
					"Couldn't determine hosts ip address.");
		}
	}

	public static void main(String[] args) {
		new Node();

	}

}
