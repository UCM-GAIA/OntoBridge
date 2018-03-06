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
 * File: Test4.java
 * 26/02/2007
 */
package ucm.gaia.ontobridge.test;

import java.util.ArrayList;

import org.apache.log4j.Level;

import ucm.gaia.ontobridge.OntoBridge;
import ucm.gaia.ontobridge.OntologyDocument;

/**
 * File used for testing the library using the GAIA photos ontology.<br>
 * This test shows how to delete properties.
 * 
 * @author Juan A. Recio Garcia
 */
public class Test9 {

	public static void main(String args[]) 
	{
		org.apache.log4j.Logger.getRootLogger().setLevel(Level.OFF);
		OntoBridge ob = new OntoBridge();
		ob.initWithPelletReasoner();
		
		OntologyDocument mainOnto = new OntologyDocument("http://gaia.fdi.ucm.es/ontologies/fotos.owl","file:test/fotos.owl");
		ArrayList<OntologyDocument> subOntologies = new ArrayList<OntologyDocument>();
		
		ob.loadOntology(mainOnto, subOntologies, false);
		
		show("Foto_1",ob);
		
		ob.deleteProperties("Foto_1","urlfoto");
		
		show("Foto_1",ob);
		
		ob.deleteOntProperty("Foto_1", "aparecePersona", "Paco");
		
		show("Foto_1",ob);
		
		ob.createDataTypeProperty("Foto_1", "lugar", "Toledo");
		
		show("Foto_1",ob);
		
		ob.deleteProperties("Foto_1","lugar");
		
		show("Foto_1",ob);
		
		
	}
	
	private static void show(String instance, OntoBridge ob)
	{
			System.out.println(instance);
			ArrayList<String> properties = new ArrayList<String>();
			ArrayList<String> values = new ArrayList<String>();
			ob.listInstancePropertiesValues(instance, properties, values);
			for(int i=0; i<properties.size(); i++)
				System.out.println(properties.get(i)+" --> "+ values.get(i));

	}
}
