package pdclogs;

import java.util.LinkedList;
import java.util.List;

import marshall.BaseClient;
import marshall.Message;

public class LogsClient implements BaseClient {

	private String url;
	
	public LogsClient(String url){
		this.url = url;
	}
	
	@Override
	public Message greet() {
		String messageHeader = "HEAD /miLog.log";
		String content = "";
		PDCLogsMessage message = new PDCLogsMessage(messageHeader, null, content);
		return message;
	}

	@Override
	public List<Message> messageReceived(Message m) {
		List<Message> list = new LinkedList<Message>();
		if(m instanceof PDCLogsMessage){
			PDCLogsMessage message = (PDCLogsMessage)m;
			System.out.println("CLIENT: "+message);
			if(message.getType().equals("HEADRESP") && message.getStatusCode() == 200){
				String messageHeader = "GET /miLog.log?0-"+message.getHeader("Lines");
				String content = "";
				PDCLogsMessage messageToSend = new PDCLogsMessage(messageHeader, null, content);
				list.add(messageToSend);
			}
		}
		return list;
	}

	@Override
	public Message createMessage(byte[] serialized) {
		PDCLogsMessage message = new PDCLogsMessage(serialized);
		return message;
	}

}
