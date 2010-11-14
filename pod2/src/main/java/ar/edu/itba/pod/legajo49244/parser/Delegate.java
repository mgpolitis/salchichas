package ar.edu.itba.pod.legajo49244.parser;

import ar.edu.itba.pod.simul.market.Resource;
import ar.edu.itba.pod.simul.simulation.Agent;

/**
 * When parsing a new command, a delegate must be provided. 
 *
 *
 */
public interface Delegate {
	
	/**
	 * This method is call when a new agent is created
	 * @param agent. Agent created
	 */
	public void handleNewAgent(Agent agent);
	
	/**
	 * This method is call when a new resource is created
	 * @param resource. Resource created
	 */
	public void handleNewResource(Resource resource);
}
