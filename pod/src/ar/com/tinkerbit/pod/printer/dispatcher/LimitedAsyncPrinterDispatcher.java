package ar.com.tinkerbit.pod.printer.dispatcher;

import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;

import ar.com.tinkerbit.pod.printer.Document;
import ar.com.tinkerbit.pod.printer.Printer;
import ar.com.tinkerbit.pod.printer.PrinterDispatcher;

public class LimitedAsyncPrinterDispatcher implements PrinterDispatcher {

	PrinterRunnable printer;
	Thread t;

	public LimitedAsyncPrinterDispatcher(int maxElems) {
		printer = new PrinterRunnable(new Printer(), maxElems);
		t = new Thread(printer);
		t.start();
	}

	@Override
	public void printDocument(Document document) {
		this.printer.addJob(document);
	}

	@Override
	public void finishPrintRequests() {

		this.printer.continueRunning = false;

		try {
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	private class PrinterRunnable implements Runnable {
		Printer printer;
		Deque<Document> queue = new LinkedBlockingDeque<Document>();
		public boolean continueRunning = true;
		private final int maxElements;

		public PrinterRunnable(Printer p, int maxElements) {
			this.maxElements = maxElements;
			this.printer = p;
		}

		public void addJob(Document d) {
			while (this.queue.size() >= this.maxElements) {
			}
			this.queue.add(d);
		}

		public void run() {
			while (continueRunning || !this.queue.isEmpty()) {
				if (!this.queue.isEmpty()) {
					this.printer.print(this.queue.poll());
				}
			}

		}

	}

	public static void main(String[] args) throws InterruptedException {
		for (int size = 9; size > 0; size--) {
			long total = 0;
			for (int run = 0; run < 10; run++) {
				LimitedAsyncPrinterDispatcher p = new LimitedAsyncPrinterDispatcher(
						size);
				long t0 = System.nanoTime();
				for (int i = 0; i < 100; i++) {
					p.printDocument(new Document(3, true, "prueba"));
				}
				p.finishPrintRequests();
				long diff = System.nanoTime() - t0;
				total += diff;
			}
			System.out.println("promedio para una cola de taman~o: " + size
					+ ">>" + total / 10 + " nanosegundos");
		}

	}

}
