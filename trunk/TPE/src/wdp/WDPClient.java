package wdp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import marshall.Reactor;
import marshall.base.BaseClient;
import marshall.model.EndPoint;
import marshall.model.Message;
import domain.DateRange;

public class WDPClient extends BaseClient {

	private final String serverHost;
	private final int serverPort;
	private Pattern messagePattern = Pattern
			.compile("(PROCESS)\\s+(pdclogs://[a-zA-Z0-9\\.]+(?::\\d{1,5})?/[a-zA-Z][a-zA-Z0-9_\\\\-]*.log(?:\\?\\d+-\\d+)?)\\s*\n(.*)",Pattern.DOTALL);
	private String logFile = null;
	private List<String> countries = null;
	private List<String> userAgents = null;
	private DateRange dates = null;

	public WDPClient(String serverHost, int serverPort) {
		super();
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
		if(headersToConvert != null){
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

	private String readGreetMessage() {
		StringBuffer aux = new StringBuffer();

		if (logFile == null) {
			// TODO: send bad request back to client
		}
		aux.append("PROCESS " + logFile + '\n');
		if (dates != null) {
			aux
					.append("DATES: " + dates.startDate + "-" + dates.endDate
							+ '\n');
		}
		if (countries != null && !countries.isEmpty()) {
			aux.append("COUNTRIES: ");
			for (String str : countries) {
				aux.append(str.trim() + ";");
			}
			aux.deleteCharAt(aux.length() - 1);
			aux.append('\n');
		}
		if (userAgents != null && !userAgents.isEmpty()) {
			aux.append("USER-AGENTS: ");
			for (String str : userAgents) {
				aux.append(str.trim() + ";");
			}
			aux.deleteCharAt(aux.length() - 1);
			aux.append('\n');
		}
		aux.append('\n');
		return aux.toString();
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
			}
			while(line != null && !line.isEmpty());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return aux.toString();
	}
	
	
	public static void main(String[] args) throws IOException {
		Reactor reactor = Reactor.getInstance();
		// worker host port + tgp host port
		// lo utiliza para indicarle al servidor a donde va a tener que
		// conectarse el director
		WDPClient c = new WDPClient("localhost", 8086);
		reactor.subscribeTCPClient(c, "localhost", 8086);
		reactor.run();
	}

}
