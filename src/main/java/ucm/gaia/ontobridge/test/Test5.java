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
 * File: Test5.java
 * 26/02/2007
 */
package ucm.gaia.ontobridge.test;

import java.util.ArrayList;
import java.util.Iterator;

import ucm.gaia.ontobridge.OntoBridge;
import ucm.gaia.ontobridge.OntologyDocument;
import ucm.gaia.ontobridge.test.gui.PnlInstancesTree;

/**
 * File used for testing the library using the GAIA photos ontology.
 * Shows a graphical representation of an instance.
 * 
 * @author Juan A. Recio Garcia
 */
public class Test5 {

	public static void main(String args[]) 
	{
		OntoBridge ob = new OntoBridge();
		ob.initWithPelletReasoner();
		
		OntologyDocument mainOnto = new OntologyDocument("http://gaia.fdi.ucm.es/ontologies/fotos.owl","file:test/fotos.owl");
		ArrayList<OntologyDocument> subOntologies = new ArrayList<OntologyDocument>();
		
		ob.loadOntology(mainOnto, subOntologies, false);
		ob.createClass("Vacaciones");
		ob.setSubClass("Vacaciones", "Foto");
		ob.createInstance("Vacaciones", "fotov1");
		ob.createOntProperty("fotov1", "aparecePersona", "Ana");
		ob.createDataTypeProperty("fotov1", "urlfoto", "file://c:/fotosvacaciones/fotov1.jpg");
		ob.createDataTypeProperty("fotov1", "fecha", "2007-02-25", "http://www.w3.org/2001/XMLSchema#date");
		
		Iterator<String> fotos = ob.listInstances("Foto");
		while(fotos.hasNext())
		{
			String instance = fotos.next();
			System.out.println(instance);
			ArrayList<String> properties = new ArrayList<String>();
			ArrayList<String> values = new ArrayList<String>();
			ob.listInstancePropertiesValues(instance, properties, values);
			for(int i=0; i<properties.size(); i++)
				System.out.println(properties.get(i)+" --> "+ values.get(i));
		}
		
		
		javax.swing.JFrame window = new javax.swing.JFrame(mainOnto.getURL() + "#Foto_1");
		PnlInstancesTree tree = new PnlInstancesTree(ob, "Foto_1");
		window.getContentPane().add(tree);
		window.pack();
		window.setSize(600, 600);
		window.setVisible(true);
		
		//ob.save("newonto.owl");
	}
}
