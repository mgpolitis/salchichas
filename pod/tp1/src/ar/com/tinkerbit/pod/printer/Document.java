package ar.com.tinkerbit.pod.printer;

import java.io.Serializable;

public class Document implements Serializable {

	private int numberOfPages;
	private boolean blackAndWhite;
	private String documentName;
	private boolean printed;

	/**
	 * Creates a new instance of a document.
	 * 
	 * @param numberOfPages
	 *            Total number of pages of this document
	 * @param blackAndWhite
	 *            True if the document has to be printed in black and white
	 * @param documentName
	 *            The name of the document
	 */
	public Document(int numberOfPages, boolean blackAndWhite,
			String documentName) {
		super();
		this.numberOfPages = numberOfPages;
		this.blackAndWhite = blackAndWhite;
		this.documentName = documentName;
		this.printed = false;
	}

	public int getNumberOfPages() {
		return this.numberOfPages;
	}

	public String getDocumentName() {
		return this.documentName;
	}

	public boolean isBlackAndWhite() {
		return this.blackAndWhite;
	}

	public void finish() {
		this.printed = true;
	}

	public boolean isPrinted() {
		return printed;
	}
}
