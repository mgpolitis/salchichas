package tgp;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import marshall.model.EndPoint;
import marshall.model.Message;

public class TGPMessage extends Message{

	private String type = "";
	private Map<String, String> content =  new HashMap<String, String>();
	
	public TGPMessage(EndPoint origin, EndPoint dest, String type, List<String> content) {
		super(origin, dest, new byte[0]);
		this.type = type;
		addContents(content);
	}
	
	public TGPMessage(String type, List<String> content){
		this(null, null, type, content);
	}
	
	public TGPMessage(byte[] serialized) {
		super(null, null, serialized);
		loadData(serialized);
	}
	
	public String getGroup(){
		return content.get("group");
	}
	
	public String getXid() {
		return content.get("xid");
	}
	
	@Override
	public void loadData(byte[] data) {
		String dataString = new String(data);
		if (!(dataString.isEmpty())) {
			int messageSeparator = dataString.indexOf('\n');
			if (messageSeparator != -1) {
				this.type = dataString.substring(0, messageSeparator);
				String[] tempArray = dataString.substring(messageSeparator)
						.split("\\n\\s*\\n");
				if (tempArray.length > 0) {
					String[] splittedContent = tempArray[0].split("\\n");
					for (String str : splittedContent) {
						addContent(str);
					}
				}
			}
		}
	}

	@Override
	public byte[] serialize() {
		String data = toString();
		return data.getBytes();
	}

	private Map<String, String> addContent(String str) {
		if (str != null) {
			String[] tempArray = str.split(":");
			if (tempArray.length == 2) {
				this.content.put(tempArray[0], tempArray[1]);
			} else {
				System.out.println("invalid key-value: " + str);
			}
		} else {
			System.out.println("invalid key-value, cannot be null ");
		}
		return content;
	}
	
	public void addContents(Collection<String> contents) {
		if (contents != null) {
			Iterator<String> it = contents.iterator();
			while (it.hasNext()) {
				addContent(it.next());
			}
		}
	}
	
	public String getContent(){
		String contentString = "";
		for (String str : this.content.keySet()) {
			contentString += str + ":" + this.content.get(str) + '\n';
		}
		return contentString;
	}
	
	public String getType(){
		return type;
	}
	
	public void setType(String type){
		this.type = type;
	}
	
	@Override
	public String toString() {
		String data = "";
		data += this.type + '\n';
		data += getContent() + '\n';
		return data;
	}

}
