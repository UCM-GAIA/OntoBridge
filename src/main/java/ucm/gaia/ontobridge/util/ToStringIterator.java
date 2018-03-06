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
 * File: ToStringIterator.java
 * 22/11/2006
 */
package ucm.gaia.ontobridge.util;

import java.util.Iterator;

/**
 * Converts a JENA iterator into an String iterator. This class allows to convert
 * between JENA classes and their corresponding URI representation in a very 
 * efficient way.
 * 
 * @author Juan A. Recio Garcia
 */
public class ToStringIterator<T> implements Iterator<T> {

	private Iterator _iter;
	
	/**
	 * Constructor.
	 * @param iter wrapped iterator which elements are converted into strings.
	 */
	public ToStringIterator(Iterator iter)
	{
		_iter = iter;
	}

	/**
	 * Indicates if there are more elements
	 */
	public boolean hasNext() {
		return _iter.hasNext();
	}

	/**
	 * Returns the following element as an string.
	 */
	@SuppressWarnings("unchecked")
	public T next() {
		return (T)_iter.next().toString();
	}

	/**
	 * Removes an element of the collection
	 */
	public void remove() {
		_iter.remove();

	}



}
