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
import domain.services.ProtocolsMessageHandler;
import domain.services.WorkerService;

public class LogsClient extends BaseClient {

	private final String serverHost;
	private final int serverPort;
	private final ProtocolsMessageHandler messageHandler;
	private String request;
	private Pattern messagePattern = Pattern
			.compile("(HEAD|GET)\\s+(/[a-zA-Z][a-zA-Z0-9_\\-]*.log(\\?(\\d+-\\d+))?)\\s*\\n");

	public LogsClient(String serverHost, int serverPort, ProtocolsMessageHandler messageHandler){
		super();
		this.serverHost = serverHost;
		this.serverPort = serverPort;
		this.messageHandler = messageHandler;
	}
	
	public void setRequest(String request){
		if(request.trim().equalsIgnoreCase("HEAD") || request.trim().equalsIgnoreCase("GET")){
			this.request = request.trim();
		}
	}
	
	@Override
	public Message greet() {
//		boolean messageOK = false;
//		String messageHeader = null;
//		while (!messageOK) {
//			String input = readGreetMessage();
//			Matcher m = messagePattern.matcher(input);
//			if (m.find()) {
//				messageOK = true;
//				messageHeader = m.group(1) + " " + m.group(2);
//			} else {
//				System.out.println("Wrong Format Message");
//			}
//		}
//		String content = "";
//		PDCLogsMessage message = new PDCLogsMessage(messageHeader, null,
//				content);
//		message.dest = new EndPoint(serverHost, serverPort);
//		System.out.println("Message Sent to Server: "+message);
		return null;
	}

	@Override
	public List<Message> messageReceived(Message m) {
		List<Message> list = new LinkedList<Message>();
		if (m instanceof PDCLogsMessage) {
			PDCLogsMessage message = (PDCLogsMessage) m;
			System.out.println("Message Received in LogsClient: " + message);
			switch (message.getStatusCode()) {
			case 200:
				if(message.getType().equals("GETRESP")){
					messageHandler.saveLogs(message.getContent());
				}
				if(message.getType().equals("HEADRESP")){
					String lines = message.getHeader("Lines");
					String contentLength = message.getHeader("Content-length");
					messageHandler.saveResourceInfo(lines,contentLength);
				}
				boolean messageOK = false;
				String messageHeader = null;
				while (!messageOK) {
					String input = readMessage();
					System.out.println("input vale en LogsClient="+input);
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

	
	public void fetchResource(String resource, String hostname, int port) throws IOException{
		PDCLogsMessage messageToSend = new PDCLogsMessage("GET "+resource,null,null);
		messageToSend.setDest(new EndPoint(hostname,port));
		this.sendMessage(messageToSend);
	}
	
	public void fetchResourceInfo(String resource, String hostname, int port) throws IOException{
		PDCLogsMessage messageToSend = new PDCLogsMessage("HEAD "+resource,null,null);
		messageToSend.setDest(new EndPoint(hostname,port));
		this.sendMessage(messageToSend);
	}
}
