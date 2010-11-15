package ar.edu.itba.pod.legajo49244.communication.payload;

import ar.edu.itba.pod.simul.communication.payload.DisconnectPayload;

public class DisconnectPayloadWalter implements DisconnectPayload {

	private static final long serialVersionUID = 7646757955224744545L;
	private final String nodeId;

	public DisconnectPayloadWalter(String nodeId) {
		this.nodeId = nodeId;
	}

	@Override
	public String getDisconnectedNodeId() {
		return this.nodeId;
	}

}
