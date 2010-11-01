/**
 * 
 */
package ar.edu.itba.pod.simul.communication;

/**
 * Types of messages
 * @author POD
 *
 */
public enum MessageType {
	NODE_AGENTS_LOAD, 				// Node agents load info
	NODE_AGENTS_LOAD_REQUEST,		// Node agents load info request to all nodes
	NODE_MARKET_DATA,				// Node market data
	NODE_MARKET_DATA_REQUEST,		// Node market data request to all nodes
	DISCONNECT, 
	RESOURCE_TRANSFER, 				// Resource been transfer from one node to another node
	RESOURCE_TRANSFER_CANCELED,		// Resource transfer from one node to another node canceled
	RESOURCE_REQUEST,				// Resource request from one node to the cluster
}
