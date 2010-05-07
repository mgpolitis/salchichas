package test.marshall;

import java.io.UnsupportedEncodingException;

import marshall.model.EndPoint;
import marshall.model.Message;

public class EchoMessage extends Message {


	String text;
	
	public EchoMessage(String message) {
		super();
		this.text = message;
	}
	
	
	public EchoMessage(EndPoint origin, EndPoint dest, byte[] data) {
		super(origin, dest, data);
	}

	@Override
	public void loadData(byte[] data) {
		try {
			this.text = new String(data, "ascii");
		} catch (UnsupportedEncodingException e) {
			System.out.println("Couldn't load data for EchoMessage");
		}

	}

	@Override
	public byte[] serialize() {
		return this.text.getBytes();
	}
	
	@Override
	public String toString() {
		return this.text;
	}

}
