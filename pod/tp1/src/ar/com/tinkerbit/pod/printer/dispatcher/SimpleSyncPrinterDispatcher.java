package ar.com.tinkerbit.pod.printer.dispatcher;

import ar.com.tinkerbit.pod.printer.Document;
import ar.com.tinkerbit.pod.printer.Printer;
import ar.com.tinkerbit.pod.printer.PrinterDispatcher;

public class SimpleSyncPrinterDispatcher implements PrinterDispatcher {

	Printer printer = new Printer();

	@Override
	public void finishPrintRequests() {
		// como los trabajos no se encolan, no hay que hacer nada
	}

	@Override
	public void printDocument(Document document) {
		this.printer.print(document);
	}

	public static void main(String[] args) throws InterruptedException {
		SimpleSyncPrinterDispatcher p = new SimpleSyncPrinterDispatcher();
		long total = 0;
		for (int run = 0; run < 10; run++) {
			long t0 = System.nanoTime();
			for (int i = 0; i<50; i++) {
				p.printDocument(new Document(10, true, "prueba"));
			}
			p.finishPrintRequests();
			long diff = System.nanoTime() - t0;
			total += diff;
			System.out.println(run+": "+diff);
		}
		System.out.println("promedio: "+total/10+" nanosegundos");
		
	}
	
}
