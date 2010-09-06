package ar.com.tinkerbit.pod.printer;

public class Printer {

	public Printer() {
		super();
	}

	/**
	 * Blocking method that prints the document
	 * @param document
	 */
	public void print(Document document) {
		for (int i = 1; i < 1 + document.getNumberOfPages(); i++) {
			printPage(document.isBlackAndWhite());
		}
	}
	
	/**
	 * Prints one page of the document. If it is a color document
	 * an extra delay is added to the print.
	 * 
	 * @param isBlackAndWhite
	 */
	protected void printPage(boolean isBlackAndWhite) {
		long delay = 1;
		if (!isBlackAndWhite) {
			delay = 5;
		}
		for (int i = 0 ; i < 1000000 * delay ; i++) {
			Math.sqrt(13.0*7);
		}
		return;
	}
}
