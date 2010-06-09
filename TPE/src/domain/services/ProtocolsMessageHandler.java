package domain.services;

import java.util.List;
import java.util.Map;

import marshall.model.EndPoint;

public interface ProtocolsMessageHandler {

	public void fetchResource(String resource, String hostname, int port);

	public void setParamsToProcess(List<String> countries,
			List<String> userAgents, String datesParam);
	
	public void saveLogs(String logs);
	
	public void saveResourceInfo(String lines, String contentLength);
	
	public void notifyWorkEnd(Map<String,Integer> results, EndPoint whoHasFinished);
	
}
