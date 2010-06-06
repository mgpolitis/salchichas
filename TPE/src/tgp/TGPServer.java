package tgp;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import marshall.base.BaseServer;
import marshall.model.EndPoint;
import marshall.model.Message;
import domain.services.DirectorService;

public class TGPServer extends BaseServer {

	private final String group;
	private final Integer MAX_RANDOM = 39591394;

	private Map<Integer, EndPoint> registry;
	private int tgpSrvPort;
	private String tgpSrvHost;
	private DirectorService directorService;

	public TGPServer(String group, String tgpSrvHost, int tgpSrvPort,
			DirectorService directorService) {
		super();
		this.group = group;
		this.tgpSrvHost = tgpSrvHost;
		this.tgpSrvPort = tgpSrvPort;
		this.directorService = directorService;
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
				System.out
						.println("Unknown message type: " + message.getType());
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


		if (xid.isEmpty()) {
			//TODO: report error: no XID
			return null;
		}

		EndPoint ep = registry.get(Integer.valueOf(xid));
		if (ep.host.equals(message.origin.host) != true) {
			//TODO: report error? fake XID (XID existed but bond to other endpoint)
			return null;
		}

		Message resp = null;
		if (groupRequest.equals(group)) {

			List<String> content = new LinkedList<String>();
			EndPoint myEndPoint = new EndPoint(tgpSrvHost, tgpSrvPort);
			resp = new TGPMessage(myEndPoint,
					message.origin, "TGPACK", content);
			directorService.startWorkingSession(ep);
			

		} else {
			// the worker is requesting to join other group, so forget him (snif...)
			registry.remove(Integer.valueOf(xid));
		}

		return resp;
	}

	private Message discoverMessage(TGPMessage message) {
		String groupRequest = message.getGroup();

		Message resp = null;

		if (groupRequest.isEmpty() || groupRequest.equals(group)) {
			// me busca a mi o a cualquiera
			List<String> content = new LinkedList<String>();

			Integer key;
			do {
				key = (int) Math.floor(Math.random() * MAX_RANDOM);
			} while (registry.get(key) != null);

			registry.put(key, message.origin);

			content.add("group: " + groupRequest);
			content.add("xid: " + key);

			resp = new TGPMessage(new EndPoint(tgpSrvHost, tgpSrvPort),
					message.origin, "TGPOFFER", content);
		}

		return resp;
	}

	/*
	 * public static void main(String[] args) throws IOException {
	 * TCPServerReactor reactor = TCPServerReactor.getInstance(); TGPServer s =
	 * new TGPServer("3","localhost",8092); reactor.subscribeUDPServer(s, 8092);
	 * reactor.runServer(); }
	 */
}
