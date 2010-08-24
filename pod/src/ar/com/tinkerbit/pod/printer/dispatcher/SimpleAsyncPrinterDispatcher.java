package ar.com.tinkerbit.pod.printer.dispatcher;

import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;

import ar.com.tinkerbit.pod.printer.Document;
import ar.com.tinkerbit.pod.printer.Printer;
import ar.com.tinkerbit.pod.printer.PrinterDispatcher;

public class SimpleAsyncPrinterDispatcher implements PrinterDispatcher {

	PrinterRunnable printer = new PrinterRunnable(new Printer());
	Thread t;
	
	public SimpleAsyncPrinterDispatcher() {
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
		
		public PrinterRunnable(Printer p) {
			this.printer = p;
		}
		
		public void addJob(Document d) {
			this.queue.add(d);
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
		SimpleAsyncPrinterDispatcher p = new SimpleAsyncPrinterDispatcher();
		long total = 0;
		for (int run = 0; run < 10; run++) {
			long t0 = System.nanoTime();
			for (int i = 0; i<50; i++) {
				p.printDocument(new Document(10, true, "prueba"));
				Thread.sleep(500);
			}
			p.finishPrintRequests();
			long diff = System.nanoTime() - t0;
			total += diff;
			System.out.println(run+": "+diff);
		}
		System.out.println("promedio: "+total/10+" nanosegundos");
		
	}
	

}
