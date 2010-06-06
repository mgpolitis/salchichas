package tgp;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import domain.services.DirectorService;

import marshall.TCPServerReactor;
import marshall.base.BaseServer;
import marshall.model.Message;
import marshall.model.EndPoint;

public class TGPServer extends BaseServer{

	private final String group;
	private final Integer MAX_RANDOM = 39591394;
	
	private Map<Integer,EndPoint> registry;
	private int tgpSrvPort;
	private String tgpSrvHost;
	private DirectorService directorService;
	
	public TGPServer(String group, String tgpSrvHost,  int tgpSrvPort, DirectorService directorService ){
		super();
		this.group = group;
		this.tgpSrvHost = tgpSrvHost;
		this.tgpSrvPort = tgpSrvPort;
		this.directorService = directorService;
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
			resp = new TGPMessage(new EndPoint(tgpSrvHost , tgpSrvPort ),
					message.origin,"TGPACK",content);
			//TODO: Establecer en la capa de arriba la conexión
			//directorService.getWdpServer().connect();
			
			registry.remove(Integer.valueOf(xid));
		}
		else{
			//TODO: Liberar recursos reservados. El usuario eligió otro director
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
			
			resp = new TGPMessage(new EndPoint(tgpSrvHost , tgpSrvPort ),
					message.origin,"TGPOFFER",content);
			// TODO: Reservar recursos para establer conexión (??)
		}
		
		return resp;
	}

	/*public static void main(String[] args) throws IOException {
		TCPServerReactor reactor = TCPServerReactor.getInstance();
		TGPServer s = new TGPServer("3","localhost",8092);
		reactor.subscribeUDPServer(s, 8092);
		reactor.runServer();
	}*/
}
