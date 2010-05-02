package pdclogs;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import marshall.BaseClient;
import marshall.Message;

public class LogsClient implements BaseClient {


	private static Pattern messagePattern = Pattern
			.compile("(HEAD|GET)\\s*(/[a-zA-Z][a-zA-Z0-9_\\-]*.log(\\?(\\d+-\\d+))?)\\s*\\n");

	@Override
	public Message greet() {
		boolean messageOK = false;
		String messageHeader = null;
		while (!messageOK) {
			String input = readStandardInput();
			Matcher m = messagePattern.matcher(input);
			if (m.find()) {
				messageOK = true;
				messageHeader = m.group(1) + " " + m.group(2);
			}
		}
		String content = "";
		PDCLogsMessage message = new PDCLogsMessage(messageHeader, null,
				content);
		return message;
	}

	@Override
	public List<Message> messageReceived(Message m) {
		List<Message> list = new LinkedList<Message>();
		if (m instanceof PDCLogsMessage) {
			PDCLogsMessage message = (PDCLogsMessage) m;
			System.out.println("CLIENT: " + message);
			boolean messageOK = false;
			String messageHeader = null;
			while (!messageOK) {
				String input = readStandardInput();
				Matcher matcher = messagePattern.matcher(input);
				if (matcher.find()) {
					messageOK = true;
					messageHeader = matcher.group(1) + " " + matcher.group(2);
				}
			}
			String content = "";
			PDCLogsMessage messageToSend = new PDCLogsMessage(messageHeader,
					null, content);
			list.add(messageToSend);
		}
		return list;
	}

	@Override
	public Message createMessage(byte[] serialized) {
		PDCLogsMessage message = new PDCLogsMessage(serialized);
		return message;
	}

	private String readStandardInput() {
		String aux = "";
		boolean newline = false;
		int c;
		try {
			while ((c = System.in.read()) != -1 && (!newline && c != '\n')) {
				aux += String.valueOf(c);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return aux;
	}

}
