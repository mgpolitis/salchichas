package ar.edu.itba.pod.legajo49244.message;

import ar.edu.itba.pod.legajo49244.main.Node;
import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.MessageType;
import ar.edu.itba.pod.simul.communication.payload.DisconnectPayload;
import ar.edu.itba.pod.simul.communication.payload.NodeAgentLoadPayload;
import ar.edu.itba.pod.simul.communication.payload.NodeAgentLoadRequestPayload;
import ar.edu.itba.pod.simul.communication.payload.NodeMarketDataPayload;
import ar.edu.itba.pod.simul.communication.payload.NodeMarketDataRequestPayload;
import ar.edu.itba.pod.simul.communication.payload.ResourceRequestPayload;
import ar.edu.itba.pod.simul.communication.payload.ResourceTransferMessagePayload;

public class Messages {

	// provides factory methods for building messages from this node
	
	public static Message newDisconnectMessage(DisconnectPayload payload) {
		return new Message(Node.getNodeId(), TimeProvider.now(), MessageType.DISCONNECT, payload);
	}

	public static Message newNodeAgentLoadResponseMessage(NodeAgentLoadPayload payload) {
		return new Message(Node.getNodeId(), TimeProvider.now(), MessageType.NODE_AGENTS_LOAD, payload);
	}
	
	public static Message newNodeAgentLoadRequestMessage(NodeAgentLoadRequestPayload payload) {
		return new Message(Node.getNodeId(), TimeProvider.now(), MessageType.NODE_AGENTS_LOAD_REQUEST, payload);
	}
	
	public static Message newNodeMarketDataResponseMessage(NodeMarketDataPayload payload) {
		return new Message(Node.getNodeId(), TimeProvider.now(), MessageType.NODE_MARKET_DATA, payload);
	}

	public static Message newNodeMarketDataRequestMessage(NodeMarketDataRequestPayload payload) {
		return new Message(Node.getNodeId(), TimeProvider.now(), MessageType.NODE_MARKET_DATA_REQUEST, payload);
	}
	
	public static Message newResourceRequestMessage(ResourceRequestPayload payload) {
		return new Message(Node.getNodeId(), TimeProvider.now(), MessageType.RESOURCE_REQUEST, payload);
	}
	
	public static Message newResourceTransferMessage(ResourceTransferMessagePayload payload) {
		return new Message(Node.getNodeId(), TimeProvider.now(), MessageType.RESOURCE_TRANSFER, payload);
	}
	
}
