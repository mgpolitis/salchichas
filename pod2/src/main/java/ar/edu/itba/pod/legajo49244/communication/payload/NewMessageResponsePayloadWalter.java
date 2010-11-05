package ar.edu.itba.pod.legajo49244.communication.payload;

import ar.edu.itba.pod.simul.communication.Message;
import ar.edu.itba.pod.simul.communication.payload.NewMessageResponsePayload;

public class NewMessageResponsePayloadWalter implements
		NewMessageResponsePayload {

	private Iterable<Message> messages;

	public NewMessageResponsePayloadWalter(Iterable<Message> messages) {
		this.messages = messages;
	}

	@Override
	public Iterable<Message> getMessages() {
		return messages;
	}

}
