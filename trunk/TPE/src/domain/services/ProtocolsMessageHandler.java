package domain.services;

import java.util.List;

public interface ProtocolsMessageHandler {

	public void fetchResource(String resource, String hostname, int port);

	public void setParamsToProcess(List<String> countries,
			List<String> userAgents, String datesParam);
	
	public void saveLogs(String logs);
	
	public void saveResourceInfo(String lines, String contentLength);
	
}
