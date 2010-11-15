package ar.edu.itba.pod.legajo49244.simulation;

import ar.edu.itba.pod.simul.market.MarketInspector;
import ar.edu.itba.pod.simul.market.MarketManager;

import com.google.common.base.Preconditions;

public class DistributedMarketManager implements MarketManager {

	private DistributedMarket market;
	private static DistributedMarketManager INSTANCE = new DistributedMarketManager();

	public static DistributedMarketManager get() {
		return INSTANCE;
	}
	
	private DistributedMarketManager() {
		market = new DistributedMarket();
	}

	@Override
	public MarketInspector inspector() {
		return market();
	}

	@Override
	public DistributedMarket market() {
		Preconditions.checkState(market != null,
				"There is no active market to be retrieved");
		return market;
	}

	@Override
	public void start() {
		market.start();
	}

	@Override
	public void shutdown() {
		market.finish();
	}
	
	

}
