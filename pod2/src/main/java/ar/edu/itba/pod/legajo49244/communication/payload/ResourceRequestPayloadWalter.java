package ar.edu.itba.pod.legajo49244.communication.payload;

import ar.edu.itba.pod.simul.communication.payload.ResourceRequestPayload;
import ar.edu.itba.pod.simul.market.Resource;

public class ResourceRequestPayloadWalter implements ResourceRequestPayload {

	private int ammountRequested;
	private Resource resource;

	public ResourceRequestPayloadWalter(int ammountRequested, Resource resource) {
		super();
		this.ammountRequested = ammountRequested;
		this.resource = resource;
	}

	@Override
	public int getAmountRequested() {
		return ammountRequested;
	}

	@Override
	public Resource getResource() {
		return resource;
	}

}
