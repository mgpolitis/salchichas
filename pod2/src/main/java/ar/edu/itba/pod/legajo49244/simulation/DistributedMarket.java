package ar.edu.itba.pod.legajo49244.simulation;

import java.util.Collection;

import ar.edu.itba.pod.simul.communication.MarketData;
import ar.edu.itba.pod.simul.market.BidInfo;
import ar.edu.itba.pod.simul.market.Market;
import ar.edu.itba.pod.simul.market.MarketInspector;
import ar.edu.itba.pod.simul.market.Resource;
import ar.edu.itba.pod.simul.market.ResourceDemand;
import ar.edu.itba.pod.simul.market.ResourceStock;

public class DistributedMarket implements Market, MarketInspector {

	@Override
	public MarketData marketData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void offer(ResourceStock stock, int maxQuantity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void offerMore(ResourceStock stock, int amount) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void request(ResourceStock stock, int maxQuantity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void requestMore(ResourceStock stock, int amount) {
		// TODO Auto-generated method stub
		
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	@Override
	public Collection<BidInfo> bidsFor(Resource resource) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int buyingCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Collection<ResourceDemand> managedResources() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int sellingCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int transactionCount() {
		// TODO Auto-generated method stub
		return 0;
	}

}
