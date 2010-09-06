package ar.com.tinkerbit.pod.printer.dispatcher;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import ar.com.tinkerbit.pod.printer.Document;
import ar.com.tinkerbit.pod.printer.Printer;
import ar.com.tinkerbit.pod.printer.PrinterDispatcher;

public class SimplePoolPrinterDispatcher implements PrinterDispatcher {

	private final DispatcherRunnable dispatcher;
	private final Thread dispatcherThread;

	public SimplePoolPrinterDispatcher(int poolSize) {
		this.dispatcher = new DispatcherRunnable(poolSize);
		Thread t = new Thread(this.dispatcher);
		t.start();
		this.dispatcherThread = t;
	}

	@Override
	public void printDocument(Document document) {
		this.dispatcher.addJob(document);
	}

	@Override
	public void finishPrintRequests() {
		this.dispatcher.end();
		while(!this.dispatcher.queue.isEmpty()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		try {
			dispatcherThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	public static class PrintRunnable implements Runnable {

		private Document document = null;
		private Printer physicalPrinter = new Printer();
		private AtomicBoolean continueRunning = new AtomicBoolean(true);

		public void end() {
			this.continueRunning.set(false);
		}

		public boolean isFree() {
			return document == null;
		}

		@Override
		public void run() {

			while (continueRunning.get() || document != null) {
				if (document != null) {
					physicalPrinter.print(document);
					this.document = null;
				}
			}

		}

		public void doPrint(Document d) {
			this.document = d;
		}

	}

	public static class DispatcherRunnable implements Runnable {

		private final List<Thread> threadPool = new LinkedList<Thread>();
		private final List<PrintRunnable> printerPool = new LinkedList<PrintRunnable>();
		private final Deque<Document> queue = new LinkedBlockingDeque<Document>();
		private AtomicBoolean continueRunning = new AtomicBoolean(true);

		public DispatcherRunnable(int poolSize) {
			for (int i = 0; i < poolSize; i++) {
				PrintRunnable r = new PrintRunnable();
				Thread t = new Thread(r);
				t.start();
				this.printerPool.add(r);
				this.threadPool.add(t);
			}

		}

		public void addJob(Document d) {
			queue.add(d);
		}

		public void end() {
			this.continueRunning.set(false);
		}

		@Override
		public void run() {
			while (continueRunning.get() || !this.queue.isEmpty()) {
				if (!this.queue.isEmpty()) {
					for (PrintRunnable r : this.printerPool) {
						if (r.isFree()) {
							Document d = this.queue.poll();
							r.doPrint(d);
							break;
						}
					}
				}
			}
			for (PrintRunnable r : this.printerPool) {
				r.end();
			}
			for (Thread t : this.threadPool) {
				try {
					t.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	public static void main(String[] args) throws InterruptedException {
		long total = 0;
		for (int run = 0; run < 10; run++) {
			long t0 = System.nanoTime();
			SimplePoolPrinterDispatcher p = new SimplePoolPrinterDispatcher(3);
			for (int i = 0; i<50; i++) {
				p.printDocument(new Document(10, true, "prueba"));
			}
			p.finishPrintRequests();
			long diff = System.nanoTime() - t0;
			total += diff;
			System.out.println(run+": "+diff);
		}
		System.out.println("promedio: "+total/10+" nanosegundos");
	
		// con 5 printers: 1000828656
		// con 4 printers: 1000645527
		// con 3 printers: 1022580347
		// con 2 printers: 1114842201
		// con 1 printers: 1800679496
		
	}
	
}
