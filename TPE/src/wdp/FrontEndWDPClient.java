package wdp;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import marshall.base.BaseClient;
import marshall.model.EndPoint;
import marshall.model.Message;

public class FrontEndWDPClient extends BaseClient {

	private final String serverHost;
	private final int serverPort;
	private Pattern messagePattern = Pattern
			.compile(
					"(PROCESS)\\s+(pdclogs://[a-zA-Z0-9\\.]+(?::\\d{1,5})?/[a-zA-Z][a-zA-Z0-9_\\\\-]*.log(?:\\?\\d+-\\d+)?)\\s*\n(.*)",
					Pattern.DOTALL);

	public FrontEndWDPClient(String serverHost, int serverPort) {
		super();
		this.serverHost = serverHost;
		this.serverPort = serverPort;
	}

	@Override
	public Message greet() {

		return null;
	}

	public Message greet2() {
		boolean messageOK = false;
		String messageHeader = null;
		List<String> headers = null;
		String headersToConvert = null;
		while (!messageOK) {
			String input = null;/*readMessage();*/ //TODO: read message
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
				Integer percentage = Integer.valueOf(message
						.getHeader("PERCENTAJE"));
				if (percentage < 0) {
					// TODO: enviar error a quien me solicito el pedido
				} else {
					System.out.println("porcentaje: " + percentage);
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

}
