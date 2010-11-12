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
import ar.edu.itba.pod.simul.simulation.SimulationManager;
import ar.edu.itba.pod.simul.time.TimeMappers;

public class Main {

	public static void main(String[] args) {

		ObjectFactory factory = new MegaFactory();
		ConnectionManager conn = null;
		if (args.length > 1) {
			conn = factory.createConnectionManager(args[0], args[1]);
		} else if (args.length > 0) {
			conn = factory.createConnectionManager(args[0]);
		} else {
			System.err.println("Must provide local IP and (optionally) entry point IP.");
		}

		MarketManager market = factory.getMarketManager(conn);
		SimulationManager simul = factory.getSimulationManager(conn,
				TimeMappers.oneSecondEach(6, TimeUnit.HOURS));
		simul.register(Market.class, market.market());
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
