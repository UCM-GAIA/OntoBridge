/**
 * OntoBride library
 * Departamento de Ingenier�a del Software e Inteligencia Artificial
 * Universidad Complutense de Madrid
 * 
 * Licensed under the terms of the GNU Library or Lesser General Public License (LGPL)
 *
 * @author Juan A. Recio Garc�a
 * 
 * This software is a subproject of the jCOLIBRI framework
 * http://sourceforge.net/projects/jcolibri-cbr/
 * http://gaia.fdi.ucm.es/projects/jcolibri/
 * 
 * File: NoLocalFileException.java
 * 22/11/2006
 */
package ucm.gaia.ontobridge.exceptions;

/**
 * Exception used when system cannot find an ontology
 * @author Juan A. Recio Garcia
 */
public class NoLocalFileException extends OntoBridgeException {
	private static final long serialVersionUID = 1L;
	
	private String _localfile;
	
	public NoLocalFileException(String localfile)
	{
		_localfile = localfile;
	}
	
	public String getMessage()
	{
		return "An ontology must be publised in a web server. If not, you can use a local copy of the ontology but in this case that copy cannot be found:\n"+_localfile+"\nBe sure you have initializated propertly the OntologyFile object\n";
	}
}
