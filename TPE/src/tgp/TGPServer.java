package tgp;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import marshall.ServerReactor;
import marshall.interfaces.BaseServer;
import marshall.model.Message;
import marshall.model.EndPoint;

public class TGPServer implements BaseServer{

	private final String group;
	private final Integer MAX_RANDOM = 39591394;
	
	private Map<Integer,EndPoint> registry;
	private String tgpSrvPort;
	private String tgpSrvHost;
	
	public TGPServer(String group, String tgpSrvPort, String tgpSrvHost){
		super();
		this.group = group;
		this.tgpSrvHost = tgpSrvHost;
		this.tgpSrvPort = tgpSrvPort;
		registry = new HashMap<Integer,EndPoint>();
	}
	
	@Override
	public Message createMessage(byte[] serialized) {
		TGPMessage message = new TGPMessage(serialized);
		return message;
	}

	@Override
	public List<Message> messageReceived(Message m) {
		List<Message> list = new LinkedList<Message>();
		Message messageToSend = null;
		if (m instanceof TGPMessage) {
			System.out.println(m);
			TGPMessage message = (TGPMessage) m;
			System.out.println("Server: " + message);
			
			if (message.getType().equals("TGPDISCOVER")) {
				messageToSend = discoverMessage(message);
				
			} else if (message.getType().equals("TGPREQUEST")) {
				messageToSend = requestMessage(message);
				
			} else {
				// TODO: unknown message
			}
		}
		if (messageToSend != null) {
			list.add(messageToSend);
		}
		return list;
	}

	private Message requestMessage(TGPMessage message) {
		String groupRequest = message.getGroup();
		String xid = message.getXid();
		
		Message resp = null;
			
		if( xid.isEmpty() ){
			return resp;
		}	
		
		EndPoint ep = registry.get(Integer.valueOf(xid));
		if(ep.host.equals(message.origin.host)!=true){
			return resp;
		}
		
		if(groupRequest.equals(group)){
			
			List<String> content = new LinkedList<String>();
			resp = new TGPMessage(new EndPoint(tgpSrvHost , Integer.valueOf(tgpSrvPort) ),
					message.origin,"TGPACK",content);
			
			registry.remove(Integer.valueOf(xid));
		}
		
		return resp;
	}

	private Message discoverMessage(TGPMessage message) {
		String groupRequest = message.getGroup();
				
		Message resp = null;
		
		if(groupRequest.isEmpty() || groupRequest.equals(group)){
			//me busca a mi o a cualquiera
			List<String> content = new LinkedList<String>();
			
			Integer key;
			do{
				key = (int)Math.floor(Math.random()*MAX_RANDOM);
			}
			while( registry.get(key) != null);
			
			registry.put(key, message.origin);
			
			content.add("group: " + groupRequest);
			content.add("xid: " + key);
			
			resp = new TGPMessage(new EndPoint(tgpSrvHost , Integer.valueOf(tgpSrvPort) ),
					message.origin,"TGPOFFER",content);
		}
		
		return resp;
	}

	public static void main(String[] args) throws IOException {
		ServerReactor reactor = ServerReactor.getInstance();
		TGPServer s = new TGPServer("3","localhost","8092");
		reactor.subscribeTCPServer(s, 8092);
		reactor.runServer();
	}
}
