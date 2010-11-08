package ar.edu.itba.pod.legajo49244.simulation;

import ar.edu.itba.pod.simul.market.MarketInspector;
import ar.edu.itba.pod.simul.market.MarketManager;

import com.google.common.base.Preconditions;

public class DistributedMarketManager implements MarketManager {

	private DistributedMarket market;
	
	

	@Override
	public MarketInspector inspector() {
		return market();
	}

	@Override
	public DistributedMarket market() {
		Preconditions.checkState(market != null, "There is no active market to be retrieved");
		return market;
	}

	@Override
	public void start() {
		market = new DistributedMarket();
		market.start();
	}
	
	@Override
	public void shutdown() {
		market.finish();
	}


}
