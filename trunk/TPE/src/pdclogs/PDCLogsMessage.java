package pdclogs;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import marshall.model.EndPoint;
import marshall.model.Message;

public class PDCLogsMessage extends Message {

	private String messageHeader = "";
	private Map<String, String> headers =  new HashMap<String, String>();
	private String content = "";

	public PDCLogsMessage(EndPoint origin, EndPoint dest, String messageHeader,
			List<String> headers, String content) {
		super(origin, dest, new byte[0]);
		this.messageHeader = messageHeader;
		this.content = content;
		addHeaders(headers);
	}

	public PDCLogsMessage(String messageHeader, List<String> headers,
			String content) {
		this(null, null, messageHeader, headers, content);
	}

	public PDCLogsMessage(byte[] serialized) {
		super(null, null, serialized);
		loadData(serialized);
	}

	@Override
	public void loadData(byte[] data) {
		if(this.headers == null){
			this.headers = new HashMap<String,String>();
		}
		String dataString = new String(data);
		if (!(dataString.isEmpty())) {
			int messageSeparator = dataString.indexOf('\n');
			if (messageSeparator != -1) {
				this.messageHeader = dataString.substring(0, messageSeparator);
				String[] tempArray = dataString.substring(messageSeparator)
						.split("\\n\\s*\\n");
				if (tempArray.length > 0) {
					String[] splittedHeaders = tempArray[0].split("\\n");
					for (String str : splittedHeaders) {
						addHeader(str);
					}
					if (tempArray.length > 1) {
						this.content = tempArray[1];
					} else {
						System.out.println("Warning: Message without content");
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

	public void addHeader(String str) {
		if (str != null) {
			String[] tempArray = str.split(":");
			if (tempArray.length == 2) {
				this.headers.put(tempArray[0], tempArray[1]);
			} else {
				System.out.println("invalid header: " + str);
			}
		} else {
			System.out.println("invalid header, cannot be null ");
		}
	}

	public void addHeaders(Collection<String> headers) {
		if (headers != null) {
			Iterator<String> it = headers.iterator();
			while (it.hasNext()) {
				addHeader(it.next());
			}
		}
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setMessageHeader(String messageHeader) {
		this.messageHeader = messageHeader;
	}

	public String getMessageHeader() {
		return this.messageHeader;
	}

	public String getHeader(String str) {
		return this.headers.get(str);
	}

	private String getHeaders() {
		String headersString = "";
		for (String str : this.headers.keySet()) {
			headersString += str + ":" + this.headers.get(str) + '\n';
		}
		return headersString;
	}

	public String getType() {
		return this.messageHeader.split(" ")[0];
	}

	public Integer getStatusCode() {
		try {
			String[] tempArray = this.messageHeader.split(" ");
			if (tempArray.length > 1) {
				String codeString = tempArray[1];
				int code = Integer.valueOf(codeString);
				return code;
			}
		} catch (Exception e) {
			// TODO no era un numero el code
		}
		return null;
	}

	public String getFileName() {
		String[] tempArray = this.messageHeader.split(" ");
		if (tempArray.length > 1) {
			String fileName = tempArray[1];
			return fileName;
		}
		return null;
	}

	@Override
	public String toString() {
		String data = "";
		data += this.messageHeader + '\n';
		data += getHeaders();
		data += '\n';
		data += this.content + '\n';
		return data;
	}
}
