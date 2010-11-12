package ar.edu.itba.pod.legajo49244;

import java.net.Inet4Address;
import java.net.UnknownHostException;

public class Node {

	private static String USER_NODE_ID = null;
	private static String USER_ENTRY_POINT = null;

	public static void setUserNodeId(String nodeId) {
		USER_NODE_ID = nodeId;
	}

	public static void setUserEntryPoint(String nodeId) {
		USER_ENTRY_POINT = nodeId;
	}

	public final static String NODE_ID = getNodeId();

	public static String getNodeId() {
		if (USER_NODE_ID != null) {
			return USER_NODE_ID;
		}

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

	public static String getEntryPoint() {
		return USER_ENTRY_POINT;
	}

}
