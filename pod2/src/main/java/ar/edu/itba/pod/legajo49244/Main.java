package ar.edu.itba.pod.legajo49244;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import ar.edu.itba.pod.legajo49244.communication.payload.Payloads;
import ar.edu.itba.pod.legajo49244.message.Messages;
import ar.edu.itba.pod.simul.ObjectFactory;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.market.Market;
import ar.edu.itba.pod.simul.market.MarketManager;
import ar.edu.itba.pod.simul.market.Resource;
import ar.edu.itba.pod.simul.simulation.Agent;
import ar.edu.itba.pod.simul.simulation.SimulationManager;
import ar.edu.itba.pod.simul.time.TimeMappers;
import ar.edu.itba.pod.simul.units.Factory;
import ar.edu.itba.pod.simul.units.SimpleConsumer;
import ar.edu.itba.pod.simul.units.SimpleProducer;

public class Main {

	public static void main(String[] args) {

		ObjectFactory factory = new MegaEpicFactory();
		ConnectionManager conn = null;
		if (args.length > 1) {
			conn = factory.createConnectionManager(args[0], args[1]);
		} else if (args.length > 0) {
			conn = factory.createConnectionManager(args[0]);
		} else {
			System.err.println("Must provide local IP and (optionally) entry point IP.");
		}

		MarketManager marketManager = factory.getMarketManager(conn);
		SimulationManager simul = factory.getSimulationManager(conn,
				TimeMappers.oneSecondEach(6, TimeUnit.HOURS));
		simul.register(Market.class, marketManager.market());
		// ...
		
		Market market = marketManager.market();
		simul.register(Market.class, market);
		
		// Define simulation agents
		Resource pigIron = new Resource("Mineral", "Pig Iron");
		Resource copper = new Resource("Mineral", "Copper");
		Resource steel = new Resource("Alloy", "Steel");
		
		if (args.length == 1 || true) {
				
			Agent mine1 = SimpleProducer.named("pig iron mine")
										.producing(2).of(pigIron)
										.every(12, TimeUnit.HOURS)
										.build();
			Agent mine2 = SimpleProducer.named("copper mine")
										.producing(4).of(copper)
										.every(1, TimeUnit.DAYS)
										.build();
			Agent refinery = Factory.named("steel refinery")
										.using(5, pigIron).and(2, copper)
										.producing(6, steel)
										.every(1, TimeUnit.DAYS)
										.build();
			Agent steelFactory = SimpleConsumer.named("factory")
										.consuming(10).of(steel)
										.every(2, TimeUnit.DAYS)
										.build();
			
			simul.addAgent(mine1);
			simul.addAgent(mine2);
			simul.addAgent(refinery);
			simul.addAgent(steelFactory);
		}
		// ...
		simul.start();
		
		
		BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
		try {
			r.readLine();

			conn.getGroupCommunication().broadcast(
					Messages.newResourceTransferMessage(Payloads
							.newResourceTransferMessagePayload("asd", "asd2",
									new Resource("cat", "name"), 8)));
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
