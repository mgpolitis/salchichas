package wdp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import marshall.Reactor;
import marshall.base.BaseClient;
import marshall.model.EndPoint;
import marshall.model.Message;
import domain.DateRange;
import domain.services.ProtocolsMessageHandler;

public class WDPClient extends BaseClient {

	private final String serverHost;
	private final int serverPort;
	private Pattern messagePattern = Pattern
			.compile(
					"(PROCESS)\\s+(pdclogs://[a-zA-Z0-9\\.]+(?::\\d{1,5})?/[a-zA-Z][a-zA-Z0-9_\\\\-]*.log(?:\\?\\d+-\\d+)?)\\s*\n(.*)",
					Pattern.DOTALL);
	private String logFile = null;
	private List<String> countries = null;
	private List<String> userAgents = null;
	private DateRange dates = null;
	private final ProtocolsMessageHandler messageHandler;

	public WDPClient(String serverHost, int serverPort, ProtocolsMessageHandler messageHandler) {
		super();
		this.messageHandler = messageHandler;
		this.serverHost = serverHost;
		this.serverPort = serverPort;
	}

	public void setLogFile(String logFile) {
		this.logFile = logFile;
	}

	public void setParameters(List<String> countries, List<String> userAgents,
			DateRange dates) {
		this.countries = countries;
		this.userAgents = userAgents;
		this.dates = dates;
	}

	@Override
	public Message greet() {
		boolean messageOK = false;
		String messageHeader = null;
		List<String> headers = null;
		String headersToConvert = null;
		while (!messageOK) {
			String input = readMessage();
			Matcher m = messagePattern.matcher(input);
			if (m.find()) {
				messageOK = true;
				messageHeader = m.group(1) + " " + m.group(2);
				headersToConvert = m.group(3);
			} else {
				System.out.println("Wrong Format Message");
			}
		}
		if (headersToConvert != null) {
			headers = Arrays.asList(headersToConvert.split("\\n"));
		}
		String content = "";
		WDPMessage message = new WDPMessage(messageHeader, headers, content);
		message.dest = new EndPoint(serverHost, serverPort);
		System.out.println("Message Sent to Server: " + message);
		return message;
	}

	@Override
	public List<Message> messageReceived(Message m) {
		List<Message> list = new LinkedList<Message>();
		if (m instanceof WDPMessage) {
			WDPMessage message = (WDPMessage) m;
			System.out.println("CLIENT: " + message);
			if (message.getType().equals("STATUS")) {
				Integer percentaje = Integer.valueOf(message
						.getHeader("PERCENTAJE"));
				if (percentaje < 0) {
					// TODO: enviar error a quien me solicito el pedido
				} else {
					// TODO: enviar estado del proceso a quien me solicito el
					// pedido
				}
			} else if (message.getType().equals("WORKDONE")) {
				Map<String,Integer> results = new HashMap<String,Integer>();
				results.put("HITS",Integer.valueOf(message.getHeader("HITS")));
				results.put("BYTES",Integer.valueOf(message.getHeader("BYTES")));
				messageHandler.notifyWorkEnd(results,message.origin);
				// TODO: obtener los resultados del trabajo procesado
				// TODO: agregar el trabajador a la lista de disponibles
			} else {
				// TODO: enviar un bad request a quien solicito el pedido
			}
		}
		return list;
	}

	@Override
	public Message createMessage(byte[] serialized) {
		WDPMessage message = new WDPMessage(serialized);
		return message;
	}


	private String readMessage() {
		StringBuffer aux = new StringBuffer();
		BufferedReader stdin = new BufferedReader(new InputStreamReader(
				System.in));

		String line;
		try {
			do {
				line = stdin.readLine();
				aux.append(line);
				aux.append('\n');
			} while (line != null && !line.isEmpty());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return aux.toString();
	}

	public void getJobDone(EndPoint server, String resource, List<String> userAgents, List<String> countries, String dateParams) throws IOException{
		List<String> headers = new ArrayList<String>();
		if(countries != null){
			String countriesString = "";
			for(String str: countries){
				countriesString += str + ";";
			}
			countriesString = countriesString.substring(0,countriesString.length()-1);
			headers.add("COUNTRIES: "+countriesString);
		}
		if(countries != null){
			String userAgentsStrings = "";
			for(String str: userAgents){
				userAgentsStrings += str + ";";
			}
			userAgentsStrings = userAgentsStrings.substring(0,userAgentsStrings.length()-1);
			headers.add("USER-AGENTS: "+userAgentsStrings);
		}
		if(dateParams != null){
			headers.add("DATES: "+dateParams);
		}
		String content = "";
		WDPMessage messageToSend = new WDPMessage("PROCESS "+resource, headers, content);
		messageToSend.setDest(server);
		this.sendMessage(messageToSend);
	}
}
