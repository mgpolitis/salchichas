package ar.com.tinkerbit.pod.rmi;

import java.rmi.Remote;

import ar.com.tinkerbit.pod.printer.Document;

public interface RemotePrinterQueue extends Remote {
	
	/**
	 * Encola un documento para la impresion
	 * @param d documento a imprimir
	 */
	void print(Document d) ;

	/**
	 * Retorna y elimina de la cola el proximo elemento encolado
	 *
	*/
	Document getNextDocument();


}
