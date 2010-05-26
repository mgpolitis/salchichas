package pdclogs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import marshall.ServerReactor;
import marshall.interfaces.BaseServer;
import marshall.model.Message;

public class LogsServer implements BaseServer {
	

	
	private String baseDirectory = ".";
	
	public LogsServer(String baseDirectory){
		super();
		this.baseDirectory = baseDirectory;
	}
	
	@Override
	public List<Message> messageReceived(Message m) {
		List<Message> list = new LinkedList<Message>();
		Message messageToSend = null;
		if (m instanceof PDCLogsMessage) {
			System.out.println(m);
			PDCLogsMessage message = (PDCLogsMessage) m;
			System.out.println("Server: " + message);
			if (message.getType().equals("HEAD")) {
				messageToSend = headRequest(message);
			} else if (message.getType().equals("GET")) {
				messageToSend = getRequest(message);
			} else {
				// TODO: unknown message
			}
		}
		if (messageToSend != null) {
			list.add(messageToSend);
		}
		return list;
	}

	private Message headRequest(PDCLogsMessage message) {
		String messageHeader = "HEADRESP ";
		String content = "";
		List<String> headers = new LinkedList<String>();
		String fileName = message.getFileName();
		if (fileName != null) {
			try {
				File archive = new File(baseDirectory + fileName);
				System.out.println(archive.getAbsolutePath());
				if (archive.exists()) {
					BufferedReader file = new BufferedReader(new FileReader(baseDirectory
							+ fileName));
					int lines = 0;
					try {
						while (file.readLine() != null) {
							lines++;
						}
						headers.add("Lines:" + lines);
						headers.add("Content-Length:" + archive.length());
						messageHeader += "200";
					} catch (IOException e) {
						e.printStackTrace();
						messageHeader += "500";
					}
				} else {
					messageHeader += "404";
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				messageHeader += "404";
			}
		} else {
			messageHeader += "406";
		}
		PDCLogsMessage responseMessage = new PDCLogsMessage(message.dest,
				message.origin, messageHeader, headers, content);
		return responseMessage;
	}

	private Message getRequest(PDCLogsMessage message) {
		String messageHeader = "GETRESP ";
		String content = "";
		List<String> headers = new LinkedList<String>();
		String fileName = message.getFileName();
		if (fileName != null) {
			String[] instructions = fileName.split("\\?");
			if (instructions.length == 2) {
				fileName = instructions[0];
				String parameter = instructions[1];
				String[] ranges = parameter.split("-");
				if (ranges.length == 2) {
					try {
						int minRange = Integer.valueOf(ranges[0]);
						int maxRange = Integer.valueOf(ranges[1]);
						if (minRange <= maxRange) {
							File archive = new File(baseDirectory + fileName);
							if (archive.exists()) {
								BufferedReader file;
								try {
									file = new BufferedReader(new FileReader(
											baseDirectory + fileName));
									int lines = 0;
									int readlines = 0;
									try {
										StringBuilder builder = new StringBuilder();
										String aux;
										while ((aux = file.readLine()) != null) {
											lines++;
											if (lines >= minRange
													&& lines <= maxRange) {
												readlines++;
												builder.append(aux);
												builder.append('\n');
											}
										}
										if (maxRange <= lines) {
											headers.add("Lines:" + readlines);
											headers.add("Content-Length:"
													+ builder.toString()
															.length());
											content = builder.toString();
											messageHeader += "200";
										} else {
											messageHeader += "406";
										}
									} catch (IOException e) {
										e.printStackTrace();
										messageHeader += "500";
									}
								} catch (FileNotFoundException e1) {
									e1.printStackTrace();
									messageHeader += "404";
								}
							} else {
								messageHeader += "404";
							}
						} else {
							messageHeader += "406";
						}
					} catch (Exception e) {
						messageHeader += "406";
					}
				} else {
					messageHeader += "406";
				}
			} else {
				messageHeader += "406";
			}
		} else {
			messageHeader += "406";
		}
		PDCLogsMessage responseMessage = new PDCLogsMessage(message.dest,
				message.origin, messageHeader, headers, content);
		return responseMessage;
	}

	@Override
	public Message createMessage(byte[] serialized) {
		PDCLogsMessage message = new PDCLogsMessage(serialized);
		return message;
	}
	
	public static void main(String[] args) throws IOException {
		ServerReactor reactor = ServerReactor.getInstance();
		LogsServer s = new 	LogsServer("./logfiles");
		reactor.subscribeTCPServer(s, 8085);
		reactor.runServer();
	}
}
