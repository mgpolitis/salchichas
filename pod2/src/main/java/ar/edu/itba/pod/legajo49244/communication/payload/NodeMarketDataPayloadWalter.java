package ar.edu.itba.pod.legajo49244.communication.payload;

import ar.edu.itba.pod.simul.communication.MarketData;
import ar.edu.itba.pod.simul.communication.payload.NodeMarketDataPayload;

public class NodeMarketDataPayloadWalter implements NodeMarketDataPayload {

	private static final long serialVersionUID = 1859134965323614476L;
	private MarketData marketData;

	public NodeMarketDataPayloadWalter(MarketData marketData) {
		this.marketData = marketData;
	}

	@Override
	public MarketData getMarketData() {
		return marketData;
	}

}
