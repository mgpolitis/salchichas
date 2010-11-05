package ar.edu.itba.pod.legajo49244.communication.payload;

import ar.edu.itba.pod.simul.communication.payload.ResourceTransferMessagePayload;
import ar.edu.itba.pod.simul.market.Resource;

public class ResourceTransferMessagePayloadWalter implements
		ResourceTransferMessagePayload {

	private int ammount;
	private String dest;
	private String src;
	private Resource resource;

	public ResourceTransferMessagePayloadWalter(int ammount, String dest,
			String src, Resource resource) {
		if (src.equals(dest)) {
			throw new IllegalArgumentException(
					"Source and destination nodes must differ.");
		}
		this.ammount = ammount;
		this.dest = dest;
		this.src = src;
		this.resource = resource;
	}

	@Override
	public int getAmount() {
		return ammount;
	}

	@Override
	public String getDestination() {
		return dest;
	}

	@Override
	public Resource getResource() {
		return resource;
	}

	@Override
	public String getSource() {
		return src;
	}

}
