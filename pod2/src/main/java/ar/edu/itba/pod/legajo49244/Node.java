package ar.edu.itba.pod.legajo49244;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.rmi.RemoteException;

import ar.edu.itba.pod.legajo49244.communication.ConnectionManagerRemote;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.communication.Message;

public class Node {

	private ConnectionManager connectionManager;

	public final static String NODE_ID = getNodeId();

	public Node() {
		connectionManager = ConnectionManagerRemote.getInstance();
		try {
			// starts message listener
			connectionManager.getGroupCommunication();
			if (NODE_ID.equals("10.6.0.167")) {
				connectionManager.getClusterAdmimnistration().createGroup();
			} else {
				connectionManager.getClusterAdmimnistration().connectToGroup("10.6.0.167");
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
		try {
			r.readLine();
			connectionManager.getGroupCommunication().broadcast(null);
		} catch (IOException e) {
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
