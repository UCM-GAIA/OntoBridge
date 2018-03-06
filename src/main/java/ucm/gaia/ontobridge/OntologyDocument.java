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
 * File: OntologyDocument.java
 * 22/11/2006
 */
package ucm.gaia.ontobridge;

import ucm.gaia.ontobridge.exceptions.NoLocalFileException;

/**
 * Represents the file/document containing an ontology.
 * As ontologies should be online but usually are placed locally this class allows to 
 * map the URI of the ontology with the local copy.
 * 
 * @author Juan A. Recio Garcia
 */
public class OntologyDocument {

	private String _URL;
	private String _localfile;
	
	
	/**
	 * Creates an ontology document
	 * @param URL The correct URL of the ontology
	 * @param localfile the local copy of the ontology
	 */
	public OntologyDocument(String URL, String localfile)
	{
		_URL = URL;
		_localfile = localfile;
	}
	
	/**
	 * Creates an ontology document. There is no local copy.
	 * @param URL The correct URL of the ontology
	 */
	public OntologyDocument(String URL)
	{
		this(URL,null);
	}

	
	/**
	 * @return the localfile
	 */
	public String getLocalfile() throws NoLocalFileException{
		if(_localfile == null)
			throw new NoLocalFileException(_localfile);
		return _localfile;
	}

	/**
	 * @return the URL
	 */
	public String getURL() {
		return _URL;
	}
	
	/**
	 * Checks if user has configured the local copy
	 */
	public boolean hasAltLocalFile()
	{
		return (_localfile != null);
	}
	
	
}
