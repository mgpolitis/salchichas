package ar.edu.itba.pod.legajo49244.simulation;

import java.rmi.RemoteException;

import ar.edu.itba.pod.legajo49244.communication.ClusterCommunicationRemote;
import ar.edu.itba.pod.legajo49244.communication.payload.Payloads;
import ar.edu.itba.pod.legajo49244.message.Messages;
import ar.edu.itba.pod.simul.local.LocalMarket;
import ar.edu.itba.pod.simul.market.Market;
import ar.edu.itba.pod.simul.market.MarketInspector;
import ar.edu.itba.pod.simul.market.Resource;
import ar.edu.itba.pod.simul.market.ResourceStock;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Multiset;

public class DistributedMarket extends LocalMarket implements Market,
		MarketInspector {

	protected final Multiset<Resource> dockStock = ConcurrentHashMultiset
			.create();

	public void exportResources(Resource resource, int ammount) {
		// do nothing, resources deleted from local simul before
	}

	public void importResources(Resource resource, int ammount) {
		dockStock.add(resource, ammount);
	}

	protected void matchBothEnds() {
		for (ResourceStock buyer : buying) {
			boolean resourcesObtained = false;
			for (ResourceStock seller : selling) {
				if (buyer.resource().equals(seller.resource())) {
					transfer(buyer, seller);
					resourcesObtained = true;
				}
			}
			for (Resource dockResource : this.dockStock) {
				if (buyer.resource().equals(dockResource)) {
					smuggle(buyer, dockResource);
					resourcesObtained = true;
				}
			}
			if (!resourcesObtained) {
				try {
					ClusterCommunicationRemote.get().broadcast(
							Messages.newResourceRequestMessage(Payloads
									.newResourceRequestPayload(buying.count(buyer),
											buyer.resource())));
				} catch (RemoteException e) {
					System.out.println("Falla el broadcast de pedido de recurso");
				}
			}
		}
	}

	private void smuggle(ResourceStock buyer, Resource dockResource) {
		while (true) {
			int wanted = buying.count(buyer);
			int available = dockStock.count(dockResource);
			int transfer = Math.min(available, wanted);

			if (transfer == 0) {
				return;
			}

			boolean procured = dockStock.setCount(dockResource, available,
					available - transfer);
			if (procured) {
				boolean sent = buying
						.setCount(buyer, wanted, wanted - transfer);
				if (sent) {
					try {
						buyer.add(transfer);
					} catch (RuntimeException e) {
						buying.remove(buyer, transfer);
						continue;
					}
					logTransfer(dockResource, buyer, transfer);

					return;
				} else {
					// Compensation. restore what we took from the order!
					dockStock.add(dockResource, transfer);
				}
			}
			// Reaching here mean we hit a race condition. Try again.
		}

	}

	private void logTransfer(Resource dockResource, ResourceStock buyer,
			int transfer) {
		transactionCount++;
		// TODO: erase
		System.out.printf("SMUGGLE: from %s to %s --> %d of %s\n", "Dock",
				buyer.name(), transfer, dockResource.name());

	}

	public boolean prepareToExportifYouHave(Resource resource, int ammount) {
		for (ResourceStock seller : selling) {
			if (resource.equals(seller.resource())) {
				boolean didPrepared = prepare(resource, ammount, seller);
				if (didPrepared) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean prepare(Resource resource, int ammount, ResourceStock seller) {
		if (selling.count(seller) >= ammount) {
			while (true) {
				int wanted = ammount;
				int available = selling.count(seller);
				int transfer = Math.min(available, wanted);

				if (transfer == 0) {
					return false;
				}

				boolean procured = selling.setCount(seller, available,
						available - transfer);
				if (procured) {
					try {
						seller.remove(transfer);
					} catch (RuntimeException e) {
						selling.add(seller, transfer);
						continue;
					}
					return true;
				}
				if (selling.count(seller) < ammount) {
					return false;
				}
				// Reaching here mean we hit a race condition. Try again.
			}
		}
		return false;
	}
	
}
