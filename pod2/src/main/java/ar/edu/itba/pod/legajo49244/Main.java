package ar.edu.itba.pod.legajo49244;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import ar.edu.itba.pod.legajo49244.communication.SimulationCommunicationRemote;
import ar.edu.itba.pod.legajo49244.main.MegaEpicFactory;
import ar.edu.itba.pod.legajo49244.message.TimeProvider;
import ar.edu.itba.pod.legajo49244.parser.Delegate;
import ar.edu.itba.pod.legajo49244.parser.InvalidCommandException;
import ar.edu.itba.pod.legajo49244.parser.SimulationNewEntityCommandParser;
import ar.edu.itba.pod.simul.ObjectFactory;
import ar.edu.itba.pod.simul.communication.ConnectionManager;
import ar.edu.itba.pod.simul.market.Market;
import ar.edu.itba.pod.simul.market.MarketManager;
import ar.edu.itba.pod.simul.market.Resource;
import ar.edu.itba.pod.simul.simulation.Agent;
import ar.edu.itba.pod.simul.simulation.SimulationManager;
import ar.edu.itba.pod.simul.time.TimeMappers;
import ar.edu.itba.pod.simul.ui.ConsoleFeedbackCallback;
import ar.edu.itba.pod.simul.ui.FeedbackCallback;
import ar.edu.itba.pod.simul.ui.FeedbackMarketManager;
import ar.edu.itba.pod.simul.ui.FeedbackSimulationManager;
import ar.edu.itba.pod.simul.units.Factory;
import ar.edu.itba.pod.simul.units.SimpleConsumer;
import ar.edu.itba.pod.simul.units.SimpleProducer;

public class Main {

	public static void main(String[] args) {

		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		FeedbackCallback callback = new ConsoleFeedbackCallback();

		ObjectFactory factory = new MegaEpicFactory();
		ConnectionManager conn = null;
		if (args.length > 1) {
			conn = factory.createConnectionManager(args[0], args[1]);
		} else if (args.length > 0) {
			conn = factory.createConnectionManager(args[0]);
		} else {
			System.err
					.println("Must provide local IP and (optionally) entry point IP.");
			System.exit(0);
		}

		MarketManager marketManager = factory.getMarketManager(conn);
		marketManager = new FeedbackMarketManager(callback, marketManager);
		final SimulationManager simul = new FeedbackSimulationManager(callback,
				factory.getSimulationManager(conn, TimeMappers.oneSecondEach(6,
						TimeUnit.HOURS)));
		simul.register(Market.class, marketManager.market());
		// ...

		Market market = marketManager.market();
		simul.register(Market.class, market);

		// Define simulation agents
		Resource pigIron = new Resource("Mineral", "Pig-Iron");
		Resource copper = new Resource("Mineral", "Copper");
		Resource steel = new Resource("Alloy", "Steel");

		if (args.length == 1) {

			Agent mine1 = SimpleProducer.named("pig iron mine").producing(2)
					.of(pigIron).every(12, TimeUnit.HOURS).build();
			Agent mine2 = SimpleProducer.named("copper mine").producing(4).of(
					copper).every(1, TimeUnit.DAYS).build();
			Agent refinery = Factory.named("steel refinery").using(5, pigIron)
					.and(2, copper).producing(6, steel).every(1, TimeUnit.DAYS)
					.build();
			Agent steelFactory = SimpleConsumer.named("factory").consuming(10)
					.of(steel).every(2, TimeUnit.DAYS).build();

			// simul.addAgent(mine1);
			// simul.addAgent(mine2);
			// simul.addAgent(refinery);
			// simul.addAgent(steelFactory);
		}
		// ...
		simul.start();

		SimulationNewEntityCommandParser parser = new SimulationNewEntityCommandParser();
		parser.addResource(pigIron);
		parser.addResource(copper);
		parser.addResource(steel);

		long start = TimeProvider.now();
		boolean rampage = false;
		BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			if (rampage && TimeProvider.now() > start + 1000 * 0.5) {
				// rampage fades in 5 seconds
				System.out.println("rampage is fading...");
				rampage = false;
			}
			try {
				String line;
				if (rampage) {
					line = "1";
				} else {
					line = r.readLine();
				}
				if (line == null) {
					return;
				}
				if (line.equals("1")) {
					line = "new agent simple-producer name=Pig-Iron-Mine producing=999 of=Pig-Iron every=12h";
				} else if (line.equals("2")) {
					line = "new agent simple-consumer name=Factory consuming=2 of=Pig-Iron every=12h";
				} else if (line.equalsIgnoreCase("rampage")) {
					System.out.println("Entering rampage mode");
					rampage = true;
					start = TimeProvider.now();
				} else if (line.equalsIgnoreCase("quit")) {
					System.out.println("Commencing QUIT.");
					simul.shutdown();
					System.exit(0);
				} else if (line.equals("loads") || line.equals("load")) {
					((SimulationCommunicationRemote) conn
							.getSimulationCommunication())
							.forcedBecomeCoordinator();
				}
				System.out.println("Command read: " + line);
				parser.parseCommand(line, new Delegate() {

					@Override
					public void handleNewResource(Resource resource) {
						System.out.println("new resource");
					}

					@Override
					public void handleNewAgent(Agent agent) {
						System.out.println("new agent");
						simul.addAgent(agent);
					}
				});

			} catch (Exception e) {
				// System.out
				// .println("new (resource | agent) parameters\n"
				// + "Creates a new resource or agent. \n"
				// + "\nResource options:"
				// + "\n\tcategory: The resource's category."
				// + "\n\tname: The resource's name"
				// +
				// "\n\n\tExample: new resource category=Mineral name=Pig-Iron\n"
				// +
				// "\nAgent options:\n\tsimple-producer\n\tsimple-consumer\n\tfactory\n"
				// + "\n\tSimple producer parameters:"
				// + "\n\t\tname: The agent's name."
				// + "\n\t\tproducing: The amount of resources to be produced."
				// + "\n\t\tof: The resource name to be produced."
				// +
				// "\n\t\tevery: The production rate. An integer followed by the time unit."
				// +
				// "\n\n\t\tExample: new agent simple-producer name=Pig-Iron-Mine producing=2 of=Pig-Iron every=12h."
				// + "\n\n\tSimple consumer parameters:"
				// + "\n\t\tname: The agent's name."
				// + "\n\t\tconsuming: The amount of resources to be produced."
				// + "\n\t\tof: The resource name to be produced."
				// +
				// "\n\t\tevery: The production rate. An integer followed by the time unit."
				// +
				// "\n\n\t\tExample: new agent simple-consumer name=Factory consuming=2 of=Pig-Iron every=12h."
				// + "\n\n\tFactory parameters:"
				// + "\n\t\tname: The agent's name."
				// +
				// "\n\t\tusing: The resource needed for production. The expected format is the amount of resources and the resource name separated with a comma."
				// + "\n\t\tproducing: The amount of resources to be produced."
				// + "\n\t\tof: The resource name to be produced."
				// +
				// "\n\t\tevery: The production rate. An integer followed by the time unit."
				// +
				// "\n\n\t\tExample: new agent factory name=Steel-Refinery using=5,Pig-Iron using=2,Copper producing=2 of=Steel every=12h."
				// + "\n\nNote:"
				// +
				// "\n\tValid Time units [Day=d, Hour=h, Minute=m, Seconds=s, MicroSeconds=ms, MicroSecconds=mms, NanoSeconds=ns]"
				// +
				// "\n\tDO NOT USE white spaces. Only to separete parameters.");
			} 
		}
	}
}
