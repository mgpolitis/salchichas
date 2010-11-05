package ar.edu.itba.pod.legajo49244.communication.payload;

import ar.edu.itba.pod.simul.communication.MarketData;
import ar.edu.itba.pod.simul.communication.payload.DisconnectPayload;
import ar.edu.itba.pod.simul.communication.payload.NodeAgentLoadPayload;
import ar.edu.itba.pod.simul.communication.payload.NodeAgentLoadRequestPayload;
import ar.edu.itba.pod.simul.communication.payload.NodeMarketDataPayload;
import ar.edu.itba.pod.simul.communication.payload.NodeMarketDataRequestPayload;
import ar.edu.itba.pod.simul.communication.payload.ResourceRequestPayload;
import ar.edu.itba.pod.simul.communication.payload.ResourceTransferMessagePayload;
import ar.edu.itba.pod.simul.market.Resource;

/**
 * Factory class for Payload objects
 * @author maraoz
 *
 */
public class Payloads {

	public static DisconnectPayload newDisconnectPayload(String nodeId) {
		return new DisconnectPayloadWalter(nodeId);
	}

	public static NodeAgentLoadPayload newNodeAgentLoadPayload(int load) {
		return new NodeAgentLoadPayloadWalter(load);
	}

	public static NodeAgentLoadRequestPayload newNodeAgentLoadRequestPayload() {
		return new NodeAgentLoadRequestPayloadWalter();
	}

	public static NodeMarketDataRequestPayload newNodeMarketDataRequestPayload() {
		return new NodeMarketDataRequestPayloadWalter();
	}

	public static NodeMarketDataPayload newNodeMarketDataPayload(
			MarketData marketData) {
		return new NodeMarketDataPayloadWalter(marketData);
	}

	public static ResourceRequestPayload newResourceRequestPayload(
			int ammountRequested, Resource resource) {
		return new ResourceRequestPayloadWalter(ammountRequested, resource);
	}

	public static ResourceTransferMessagePayload newResourceTransferMessagePayload(
			String src, String dest, Resource resource, int ammount) {
		return new ResourceTransferMessagePayloadWalter(ammount, dest, src,
				resource);
	}

}
