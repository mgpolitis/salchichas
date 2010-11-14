package ar.edu.itba.pod.legajo49244.parser;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ar.edu.itba.pod.simul.market.Resource;
import ar.edu.itba.pod.simul.simulation.Agent;
import ar.edu.itba.pod.simul.units.Factory;
import ar.edu.itba.pod.simul.units.SimpleConsumer;
import ar.edu.itba.pod.simul.units.SimpleProducer;
import ar.edu.itba.pod.simul.units.Factory.FactoryBuilder;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class SimulationNewEntityCommandParser {

	private static final int flags = Pattern.DOTALL | Pattern.CASE_INSENSITIVE;
	private static final Pattern newCommandPattern = Pattern.compile("new\\s*(agent|resource)\\s*(.*$)", flags);
	private static final Pattern newAgentPattern = Pattern.compile("(simple-consumer|simple-producer|factory)\\s*(.*$)", flags);
	private static final Map<String, TimeUnit> timeUnits = Maps.newHashMap();
	private final Map<String, Resource> resources = Maps.newHashMap();
	private final List<Agent> agents = Lists.newLinkedList();
	private final Map<String, Parser<? extends Agent>> agentParsers = Maps.newHashMap();


	static {
		timeUnits.put("h", TimeUnit.DAYS);
		timeUnits.put("m", TimeUnit.HOURS);
		timeUnits.put("s", TimeUnit.SECONDS);
		timeUnits.put("ms", TimeUnit.MILLISECONDS);
		timeUnits.put("mms", TimeUnit.MICROSECONDS);
		timeUnits.put("ns", TimeUnit.NANOSECONDS);
	}

	private interface Parser<T> {
		T parse(String parameters) throws InvalidCommandException;
	}

	private class FactoryParser implements Parser<Agent> {
		private final String FACTORY_AGENT_PARAMETERS_REGEX = "\\s*([^=]*)=([^\\s]*)"; 
		private final Pattern factoryPattern = Pattern.compile(FACTORY_AGENT_PARAMETERS_REGEX, flags);

		@SuppressWarnings("unchecked")
		@Override
		public Agent parse(final String parameters) throws InvalidCommandException {
			Matcher paramMatcher = factoryPattern.matcher(parameters);
			Map<String, Object> paramMap = SimulationNewEntityCommandParser.parseAgentParameters(paramMatcher);
			String name = (String) paramMap.get("name");
			Integer producing = (Integer) paramMap.get("producing");
			String resourceName = (String) paramMap.get("of");
			Object[] every = (Object[]) paramMap.get("every");
			List<Object[]> using = (List<Object[]>) paramMap.get("using");

			if (name == null) {
				throw new InvalidCommandException("The parameter name was not supplied");
			}
			if (producing == null) {
				throw new InvalidCommandException("The parameter producing was not supplied");
			}
			if (resourceName == null) {
				throw new InvalidCommandException("The parameter resource was not supplied");
			}
			if (every == null) {
				throw new InvalidCommandException("The parameter every was not supplied");
			}
			if (using == null) {
				throw new InvalidCommandException("The parameter using was not supplied");
			}
			Resource resource = resources.get(resourceName);
			if (resource == null) {
				throw new InvalidCommandException("The resource " + resourceName + " does not exists.");
			}
			if (using.size() < 1) {
				throw new InvalidCommandException("The factory must use at least one resource");
			}
			for (Object[] objects : using) {
				if (resources.get(objects[1]) == null) {
					throw new InvalidCommandException("The resource " + resourceName + " does not exists.");
				}
			}

			FactoryBuilder factory = Factory.named(name);
			factory.producing(producing, resource);
			factory.every((Integer) every[0], (TimeUnit) every[1]);
			Object[] usingResource = using.get(0);
			factory.using((Integer) usingResource[0], resources.get((String) usingResource[1]));
			for (int i = 1; i < using.size(); ++i) {
				usingResource = using.get(i);
				factory.and((Integer) usingResource[0], resources.get((String) usingResource[1]));
			}

			return factory.build();
		}
	}

	private class SimpleConsumerParser implements Parser<Agent> {
		private final String SIMPLE_CONSUMER_AGENT_PARAMETERS_REGEX = "\\s*([^=]*)=([^\\s]*)";	
		private final Pattern simpleConsumerPattern = Pattern.compile(SIMPLE_CONSUMER_AGENT_PARAMETERS_REGEX, flags);

		@Override
		public Agent parse(final String parameters) throws InvalidCommandException {
			Matcher paramMatcher = simpleConsumerPattern.matcher(parameters);
			Map<String, Object> paramMap = SimulationNewEntityCommandParser.parseAgentParameters(paramMatcher);
			String name = (String) paramMap.get("name");
			Integer consuming = (Integer) paramMap.get("consuming");
			String resourceName = (String) paramMap.get("of");
			Object[] every = (Object[]) paramMap.get("every");

			if (name == null) {
				throw new InvalidCommandException("The parameter name was not supplied");
			}
			if (consuming == null) {
				throw new InvalidCommandException("The parameter producing was not supplied");
			}
			if (resourceName == null) {
				throw new InvalidCommandException("The parameter resource was not supplied");
			}
			if (every == null) {
				throw new InvalidCommandException("The parameter every was not supplied");
			}
			Resource resource = resources.get(resourceName);
			if (resource == null) {
				throw new InvalidCommandException("The resource " + resourceName + " does not exists.");
			}

			return SimpleConsumer.named(name)
			.consuming(consuming)
			.of(resource)
			.every((Integer) every[0], (TimeUnit) every[1])
			.build();
		}
	}

	private class SimpleProducerParser implements Parser<Agent> {
		//private final String SIMPLE_PRODUCER_AGENT_PARAMETERS_REGEX = "(?:(?:(name)=([^\\s]+))|(?:(of)=([^\\s]+))|(?:(every)=(\\d+(?:d|h|m|s|ms|mms|ns)))|(?:(producing)=(\\d+)))";
		private final String SIMPLE_PRODUCER_AGENT_PARAMETERS_REGEX = "\\s*([^=]*)=([^\\s]*)";
		private final Pattern simpleProducerPattern = Pattern.compile(SIMPLE_PRODUCER_AGENT_PARAMETERS_REGEX, flags);

		@Override
		public Agent parse(final String parameters) throws InvalidCommandException {
			Matcher paramMatcher = simpleProducerPattern.matcher(parameters);
			Map<String, Object> paramMap = SimulationNewEntityCommandParser.parseAgentParameters(paramMatcher);
			String name = (String) paramMap.get("name");
			Integer producing = (Integer) paramMap.get("producing");
			String resourceName = (String) paramMap.get("of");
			Object[] every = (Object[]) paramMap.get("every");

			if (name == null) {
				throw new InvalidCommandException("The parameter name was not supplied");
			}
			if (producing == null) {
				throw new InvalidCommandException("The parameter producing was not supplied");
			}
			if (resourceName == null) {
				throw new InvalidCommandException("The parameter resource was not supplied");
			}
			if (every == null) {
				throw new InvalidCommandException("The parameter every was not supplied");
			}
			Resource resource = resources.get(resourceName);
			if (resource == null) {
				throw new InvalidCommandException("The resource " + resourceName + " does not exists.");
			}

			return SimpleProducer.named(name)
			.producing(producing)
			.of(resource)
			.every((Integer) every[0], (TimeUnit) every[1])
			.build();
		}
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> parseAgentParameters(final Matcher matcher) throws InvalidCommandException {
		Preconditions.checkNotNull(matcher, "The matcher cannot be null");
		Map<String, Object> params = Maps.newHashMap();
		try {
			while (matcher.find()) {
				String key = matcher.group(1);
				if (key.equals("name") || key.equals("of") ){
					params.put(key, matcher.group(2));
				} else if (key.equals("consuming") || key.equals("producing")) {
					params.put(key, Integer.valueOf(matcher.group(2)));
				} else if (key.equals("every")) {
					String value = matcher.group(2);
					Matcher stringMatcher = Pattern.compile("(\\d+)(\\w+)").matcher(value);
					if(stringMatcher.find()){
						Integer integer = Integer.valueOf(stringMatcher.group(1));
						String string = stringMatcher.group(2);
						params.put(key, new Object[]{integer, timeUnits.get(string)});
					}
				} else if (key.equals("using")) {
					List<Object[]> values = (List<Object[]>) params.get(key);
					if (values == null ){
						values = Lists.newArrayList();
					}
					String[] str = matcher.group(2).split(",");
					values.add(new Object[]{Integer.valueOf(str[0]), str[1]});
					params.put(key, values);
				}
			}
		} catch (Exception e) {
			throw new InvalidCommandException("The command was not valid " + e.getMessage());
		}
		return params;
	}

	public SimulationNewEntityCommandParser() {
		this.agentParsers.put("factory", new FactoryParser());
		this.agentParsers.put("simple-consumer", new SimpleConsumerParser());
		this.agentParsers.put("simple-producer", new SimpleProducerParser());
	}

	public void parseCommand(final String command, final Delegate delegate) throws InvalidCommandException {
		Preconditions.checkNotNull(command, "The command cannot be null.");
		Preconditions.checkArgument(!command.isEmpty(), "The command cannot be empty string");

		try{
			Matcher newCommandMatcher = newCommandPattern.matcher(command);
			if ( newCommandMatcher.find() ){
				String commandType = newCommandMatcher.group(1);
				if ( commandType.equals("agent") ) {
					delegate.handleNewAgent(parseAgent(newCommandMatcher.group(2)));
				}
				else if( commandType.equals("resource") )
					delegate.handleNewResource(parseResource(newCommandMatcher.group(2)));
				else
					throw new InvalidCommandException("The command type " + commandType + " is not valid");
			}
		} catch(Exception e) {
			throw new InvalidCommandException("There was an unexpected exception: "+e.getMessage(),e);
		}
	}
	
	public void addResource(final Resource resource) {
		Preconditions.checkNotNull(resource, "The resource cannot be null.");
		resources.put(resource.name(), resource);
	}
	
	public Collection<Agent> agents() {
		return agents;
	}

	public Collection<Resource> resources() {
		return resources.values();
	}
	
	public Resource getResource(String name) {
		Preconditions.checkNotNull(name, "The resource name cannot be null");
		return resources.get(name);
	}

	private Agent parseAgent(final String command) throws InvalidCommandException {
		Matcher newAgentMatcher = newAgentPattern.matcher(command);
		Agent agent = null;
		if(newAgentMatcher.find()){
			String agentType = newAgentMatcher.group(1);
			Parser<? extends Agent> agentParser = agentParsers.get(agentType);


			if (agentParser == null) {
				throw new InvalidCommandException("The agent type " + agentType + " is not valid");
			} else {
				agent = agentParser.parse(newAgentMatcher.group(2));
			}
		}
		agents.add(agent);
		return agent;
	}

	private Resource parseResource(final String command) throws InvalidCommandException{
		Pattern resourceProp = Pattern.compile("(category|name)=([a-zA-Z\\-]+)");
		Map<String, String> properties = Maps.newHashMap();
		Matcher m = resourceProp.matcher(command);
		while (m.find()) {
			properties.put(m.group(1), m.group(2));
		}

		String category = properties.get("category");
		String name = properties.get("name");
		if (category == null) {
			throw new InvalidCommandException("The resource's category must be provided");
		}
		if (name == null) {
			throw new InvalidCommandException("The resource's name must be provided");
		}
		Resource resource = new Resource(category, name);
		resources.put(resource.name(), resource);
		return resource;
	}
}
