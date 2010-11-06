package ar.edu.itba.pod.legajo49244;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.RemoteException;

import ar.edu.itba.pod.legajo49244.communication.ConnectionManagerRemote;
import ar.edu.itba.pod.legajo49244.communication.payload.ResourceTransferMessagePayloadWalter;
import ar.edu.itba.pod.legajo49244.message.Messages;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.market.Resource;

public class Main {

	public static void main(String[] args) {

		if (args.length > 0) {
			System.out.println("User set my nodeId to " + args[0]);
			Node.setUserNodeId(args[0]);
		}

		if (args.length > 1) {
			System.out.println("User set my entry point to cluster as "
					+ args[1]);
			Node.setUserEntryPoint(args[1]);
		}

		ConnectionManager connectionManager = ConnectionManagerRemote
				.getInstance();
		System.out.println("My node id is " + Node.getNodeId());

		try {
			if (Node.getEntryPoint() == null) {
				connectionManager.getClusterAdmimnistration().createGroup();
			} else {
				connectionManager.getClusterAdmimnistration().connectToGroup(
						Node.getEntryPoint());
			}

		} catch (RemoteException e) {
			System.out.println(e.getMessage());
			System.out.println("There was a problem joining the cluster, now exiting");
			System.exit(0);
		}

		BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
		try {
			r.readLine();

			connectionManager
					.getGroupCommunication()
					.broadcast(
							Messages
									.newResourceTransferMessage(new ResourceTransferMessagePayloadWalter(
											10, "a", "b", new Resource("cat",
													"name"))));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
