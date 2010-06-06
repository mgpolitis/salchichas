package pdclogs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import marshall.base.BaseClient;
import marshall.model.EndPoint;
import marshall.model.Message;
import domain.services.WorkerService;

public class LogsClient extends BaseClient {

	private final String serverHost;
	private final int serverPort;
	private final WorkerService workerService;
	private Pattern messagePattern = Pattern
			.compile("(HEAD|GET)\\s+(/[a-zA-Z][a-zA-Z0-9_\\-]*.log(\\?(\\d+-\\d+))?)\\s*\\n");

	public LogsClient(String serverHost, int serverPort, WorkerService workerService){
		super();
		this.serverHost = serverHost;
		this.serverPort = serverPort;
		this.workerService = workerService;
	}
	
	
	@Override
	public Message greet() {
		boolean messageOK = false;
		String messageHeader = null;
		while (!messageOK) {
			String input = readMessage();
			Matcher m = messagePattern.matcher(input);
			if (m.find()) {
				messageOK = true;
				messageHeader = m.group(1) + " " + m.group(2);
			} else {
				System.out.println("Wrong Format Message");
			}
		}
		String content = "";
		PDCLogsMessage message = new PDCLogsMessage(messageHeader, null,
				content);
		message.dest = new EndPoint(serverHost, serverPort);
		System.out.println("Message Sent to Server: "+message);
		return message;
	}

	@Override
	public List<Message> messageReceived(Message m) {
		List<Message> list = new LinkedList<Message>();
		if (m instanceof PDCLogsMessage) {
			PDCLogsMessage message = (PDCLogsMessage) m;
			System.out.println("CLIENT: " + message);
			switch (message.getStatusCode()) {
			case 200:
				if(message.getType().equals("GETRESP")){
					workerService.saveLogs(message.getContent());
				}
				boolean messageOK = false;
				String messageHeader = null;
				while (!messageOK) {
					String input = readMessage();
					System.out.println(input);
					Matcher matcher = messagePattern.matcher(input);
					if (matcher.find()) {
						messageOK = true;
						messageHeader = matcher.group(1) + " " + matcher.group(2);
					}
				}
				String content = "";
				PDCLogsMessage messageToSend = new PDCLogsMessage(messageHeader,
						null, content);
				messageToSend.dest = new EndPoint(serverHost, serverPort);
				list.add(messageToSend);
				break;
//			case 404:
//				workerDao.getWdpServer().sendLogNotFound();
//				break;
//			case 405:
//				workerDao.getWdpServer().sendInvalidRange();
//				break;
//			case 406:
//				workerDao.getWdpServer().sendBadRequest();
//				break;	
//			default:
//				workerDao.getWdpServer().sendUnknownError();
//				break;
			}
		}
		return list;
	}

	@Override
	public Message createMessage(byte[] serialized) {
		PDCLogsMessage message = new PDCLogsMessage(serialized);
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
			}
			while(line != null && !line.isEmpty());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return aux.toString();
	}

}
