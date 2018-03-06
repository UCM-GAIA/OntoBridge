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
 * File: Test2gui.java
 * 26/02/2007
 */
package ucm.gaia.ontobridge.test;

import java.util.ArrayList;

import ucm.gaia.ontobridge.OntoBridge;
import ucm.gaia.ontobridge.OntologyDocument;
import ucm.gaia.ontobridge.test.gui.PnlConceptsTree;

/**
 * File used for testing the library using the GAIA restaurants ontology.
 * Shows a graphical representation of the ontology (Classes only)
 * http://gaia.fdi.ucm.es/ontologies/
 * 
 * @author Juan A. Recio Garcia
 */
public class Test2gui {

	public static void main(String args[]) 
	{
		OntoBridge ob = new OntoBridge();
		ob.initWithPelletReasoner();
		
		OntologyDocument mainOnto = new OntologyDocument("http://gaia.fdi.ucm.es/ontologies/restaurant.owl","file:test/restaurant.owl");
		
		ArrayList<OntologyDocument> subOntologies = new ArrayList<OntologyDocument>();
		subOntologies.add(new OntologyDocument("http://gaia.fdi.ucm.es/ontologies/calendar.owl","file:test/calendar.owl"));
		subOntologies.add(new OntologyDocument("http://gaia.fdi.ucm.es/ontologies/price.owl","file:test/price.owl"));
		subOntologies.add(new OntologyDocument("http://gaia.fdi.ucm.es/ontologies/address.owl","file:test/address.owl"));
		subOntologies.add(new OntologyDocument("http://gaia.fdi.ucm.es/ontologies/contact-details.owl","file:test/contact-details.owl"));
		
		ob.loadOntology(mainOnto, subOntologies, false);
		
		javax.swing.JFrame window = new javax.swing.JFrame(mainOnto.getURL());
		PnlConceptsTree tree = new PnlConceptsTree(ob);
		window.getContentPane().add(tree);
		window.pack();
		window.setSize(300, 600);
		window.setVisible(true);
		
	}
}
