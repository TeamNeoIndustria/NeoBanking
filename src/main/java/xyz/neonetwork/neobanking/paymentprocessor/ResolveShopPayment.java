package xyz.neonetwork.neobanking.paymentprocessor;

import xyz.neonetwork.neobanking.NeoBanking;
import xyz.neonetwork.neobanking.networking.IRSTransaction;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class ResolveShopPayment {

	private static List<RequestResolverQueueItem> requestResolverQueue = new ArrayList<>();

	@FunctionalInterface
	public interface RequestResolverCallback {
		void onComplete(String response);
	}

//	public static void addToRequestResolverQueue(IRSTransaction transaction, RequestResolverCallback callback) {
	public static void addToRequestResolverQueue(IRSTransaction transaction) {
//		requestResolverQueue.add();
		//		requestResolverQueue.add(item);
	}

	public static void processRequestResolverQueue() {
		if (requestResolverQueue.isEmpty()) return;
		ListIterator<RequestResolverQueueItem> iterator = requestResolverQueue.listIterator();
		while(iterator.hasNext()) {
			RequestResolverQueueItem item = iterator.next();
			if (true) NeoBanking.LOGGER.info("Hi");
			iterator.remove();
		}
	}

}
