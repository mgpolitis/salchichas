package domain.services;

import java.util.List;

public interface WDPServerMessageHandler {

	public void fetchResource(String resource, String hostname, int port);

	public void setParamsToProcess(List<String> countries,
			List<String> userAgents, String datesParam);
}
