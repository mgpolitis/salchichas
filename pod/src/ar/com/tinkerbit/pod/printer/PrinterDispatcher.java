package ar.com.tinkerbit.pod.printer;

public interface PrinterDispatcher {

	/**
	 * Sends the request for a new print job of the document.
	 * 
	 * @param document
	 */
	public void printDocument(Document document);
	
	/**
	 * Blocking method called to ensure that all the print jobs
	 * sent to the dispatcher are finished. Only after all the jobs
	 * are printed, the dispatcher returns from this method.
	 */
	public void finishPrintRequests();
}
