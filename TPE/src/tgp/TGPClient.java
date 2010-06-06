package tgp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import marshall.base.BaseClient;
import marshall.model.EndPoint;
import marshall.model.Message;
import domain.Configuration;
import domain.services.WorkerService;

public class TGPClient extends BaseClient {
	private String tgpCliHost;
	private int tgpCliPort;
	private WorkerService workerService;
	private State state;
	private int group;

	private static Pattern groupPattern = Pattern.compile("[0-9]+\\n");

	private enum State {
		DISCOVERING, WAITING_OFFER, WAITING_ACK, SUBSCRIBED
	};

	private final ScheduledExecutorService timeOutScheduler = Executors
			.newSingleThreadScheduledExecutor();
	private final TGPClient thiz = this;
	private final Runnable restartRunner = new Runnable() {
		@Override
		public void run() {
			thiz.state = State.DISCOVERING;
			Message m = thiz.greet();
			try {
				thiz.sendMessage(m);
			} catch (IOException e) {
				System.out.println("Cannot send TGPDISCOVER on timeout");
				e.printStackTrace();
			}
		}
	};

	public TGPClient(String tgpCliHost, int tgpCliPort,
			WorkerService workerService) {
		super();
		this.state = State.DISCOVERING;
		this.tgpCliHost = tgpCliHost;
		this.tgpCliPort = tgpCliPort;
		this.workerService = workerService;
	}

	@Override
	public Message createMessage(byte[] serialized) {
		TGPMessage message = new TGPMessage(serialized);
		return message;
	}

	@Override
	public Message greet() {
		// TGPDISCOVER

		String messageContent = null;

		List<String> c = new ArrayList<String>();

		String input = readMessage();

		Matcher m = groupPattern.matcher(input);
		if (m.find()) {
			messageContent = "group: " + m.group(1);
			c.add(messageContent);
		} else {
			System.out.println("No group specified");
		}

		TGPMessage message = new TGPMessage("TGPDISCOVER", c);

		state = State.WAITING_OFFER;

		message.broadcastMe = true;
		message.origin = new EndPoint(this.tgpCliHost, this.tgpCliPort);

		System.out.println("Message Sent to Server: " + message);
		this.setCountdownForRestart();
		return message;
	}

	private String readMessage() {
		StringBuffer aux = new StringBuffer();
		BufferedReader stdin = new BufferedReader(new InputStreamReader(
				System.in));
		System.out.println("Ingresar numero de grupo o dejar vacio:");
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

	@Override
	public List<Message> messageReceived(Message m) {
		List<Message> list = null;
		Message messageToSend = null;
		if (m instanceof TGPMessage) {
			TGPMessage message = (TGPMessage) m;
			System.out.println("CLIENT: " + message);

			if (message.getType().equals("TGPOFFER")
					&& state == State.WAITING_OFFER) {
				messageToSend = offerReceived(message);
			} else if (message.getType().equals("TGPACK")) {
				if (state == State.WAITING_ACK) {
					state = State.SUBSCRIBED;
					this.workerService.setGroup(this.group);
				} // No hay respuesta al ACK

			} else {
				// unknown message format
			}

			if (messageToSend != null) {
				list = new LinkedList<Message>();
				list.add(messageToSend);
			}

		}

		return list;
	}

	private Message offerReceived(TGPMessage message) {
		List<String> content = new ArrayList<String>();
		String respGroup = message.getGroup();
		String xid = message.getXid();

		if (respGroup.isEmpty() || xid.isEmpty()) {
			return null; // TODO: Mensaje mal formado.
		}
		content.add("group: " + respGroup);
		content.add("host: " + this.workerService.getWorkerHost());
		content.add("port: " + this.workerService.getWorkerPort());
		content.add("xid: " + xid);
		this.group = Integer.valueOf(respGroup);

		TGPMessage messageToSend = new TGPMessage("TGPREQUEST", content);

		this.setCountdownForRestart();

		messageToSend.broadcastMe = true;
		messageToSend.origin = new EndPoint(this.tgpCliHost, this.tgpCliPort);
		this.setCountdownForRestart();
		return messageToSend;
	}

	private void setCountdownForRestart() {
		timeOutScheduler.schedule(restartRunner, Configuration.TGP_TIMEOUT,
				TimeUnit.SECONDS);
	}

}
