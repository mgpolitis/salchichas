package ar.edu.itba.pod.legajo49244.communication.payload;

import ar.edu.itba.pod.simul.communication.payload.NodeAgentLoadPayload;

public class NodeAgentLoadPayloadWalter implements NodeAgentLoadPayload {

	int load;

	public NodeAgentLoadPayloadWalter(int load) {
		this.load = load;
	}

	@Override
	public int getLoad() {
		return load;
	}

}
