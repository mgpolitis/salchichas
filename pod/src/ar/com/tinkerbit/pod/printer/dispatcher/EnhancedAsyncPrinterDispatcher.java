package ar.com.tinkerbit.pod.printer.dispatcher;

import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;

import ar.com.tinkerbit.pod.printer.Document;
import ar.com.tinkerbit.pod.printer.Printer;
import ar.com.tinkerbit.pod.printer.PrinterDispatcher;
import ar.com.tinkerbit.pod.util.TimeOutException;

public class EnhancedAsyncPrinterDispatcher implements PrinterDispatcher {


	PrinterRunnable printer;
	Thread t;
	final long timeout;
	
	public EnhancedAsyncPrinterDispatcher(long timeout) {
		printer =  new PrinterRunnable(new Printer(), 5);
		t = new Thread(printer);
		t.start();
		
		this.timeout = timeout;
	}

	@Override
	public void printDocument(Document document) {
		this.printer.addJob(document, timeout);
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
		
		public void addJob(Document d, long timeout) {
			long t0 = System.nanoTime();
			boolean added = false;
			while(!added && System.nanoTime()-t0 < timeout) {
				if (this.queue.size() < this.maxElements) {
					this.queue.add(d);
					added = true;
				}
			}
			if (!added)
				throw new TimeOutException();
			
		}

		public void run() {
			while(continueRunning || !this.queue.isEmpty()) {
				Document d = this.queue.poll();
				if (d != null) {
					this.printer.print(d);
				}
			}

		}

	}
	
	
	
	
	
	
	
	public static void main(String[] args) throws InterruptedException {
		long total = 0;
		for (int run = 0; run < 10; run++) {
			EnhancedAsyncPrinterDispatcher p = new EnhancedAsyncPrinterDispatcher(100000000);
			long t0 = System.nanoTime();
			for (int i = 0; i<50; i++) {
				try {
					p.printDocument(new Document(10, true, "prueba"));
				} catch (TimeOutException e) {
					e.printStackTrace();
				}
				
			}
			p.finishPrintRequests();
			long diff = System.nanoTime() - t0;
			total += diff;
			System.out.println("run "+run+": "+diff);
		}
		System.out.println("promedio: "+total/10+" nanosegundos");
		
	}

}
