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
 * File: Test1.java
 * 22/11/2006
 */
package ucm.gaia.ontobridge.test;

import java.util.ArrayList;
import java.util.Iterator;

import ucm.gaia.ontobridge.OntoBridge;
import ucm.gaia.ontobridge.OntologyDocument;

/**
 * File used for testing the library using the GAIA restaurants ontology.
 * http://gaia.fdi.ucm.es/ontologies/
 * 
 * @author Juan A. Recio Garcia
 */
public class Test1 {

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
		
		System.out.println("\nALL CLASSES (no anonymous)");
		for(Iterator<String> iter = ob.listAllClasses(); iter.hasNext();)
			System.out.println(iter.next());
		
		System.out.println("\nHIERARCHY ROOT CLASSES (no anonymous)");
		for(Iterator<String> iter = ob.listRootClasses(); iter.hasNext();)
			System.out.println(iter.next());
		
		System.out.println("\nDIRECT SUBCLASSES OF http://gaia.fdi.ucm.es/ontologies/restaurant.owl#Facility");
		for(Iterator<String> iter = ob.listSubClasses("http://gaia.fdi.ucm.es/ontologies/restaurant.owl#Facility", true); iter.hasNext();)
			System.out.println(iter.next());
		
		System.out.println("\nDIRECT AND INDIRECT SUBCLASSES OF http://gaia.fdi.ucm.es/ontologies/restaurant.owl#Facility");
		for(Iterator<String> iter = ob.listSubClasses("http://gaia.fdi.ucm.es/ontologies/restaurant.owl#Facility", false); iter.hasNext();)
			System.out.println(iter.next());
		
		System.out.println("\nDIRECT SUPERCLASSES OF http://gaia.fdi.ucm.es/ontologies/restaurant.owl#ChildrenMeal");
		for(Iterator<String> iter = ob.listSuperClasses("http://gaia.fdi.ucm.es/ontologies/restaurant.owl#ChildrenMeal", true); iter.hasNext();)
			System.out.println(iter.next());
		
		System.out.println("\nDIRECT AND INDIRECT SUBCLASSES OF http://gaia.fdi.ucm.es/ontologies/restaurant.owl#ChildrenMeal");
		for(Iterator<String> iter = ob.listSuperClasses("ChildrenMeal", false); iter.hasNext();)
			System.out.println(iter.next());
		
		System.out.println("\nIs Children Meal subclass of Facility?");
		System.out.println(ob.isSubClassOf("ChildrenMeal", "Facility"));
		
		System.out.println("\nIs Restaurant_2 instance of Facility?");
		System.out.println(ob.isInstanceOf("Restaurant_2", "Facility"));
		
		System.out.println("\nProperties applicable to Restaurant");
		for(Iterator<String> iter = ob.listProperties("Restaurant"); iter.hasNext();)
			System.out.println(iter.next());
		
		System.out.println("\nSpecific properties for Restaurant (they have Restaurant as domain)");
		for(Iterator<String> iter = ob.listSpecificProperties("Restaurant"); iter.hasNext();)
			System.out.println(iter.next());
		
		System.out.println("\nRANGE OF openingPeriods");
		for(Iterator<String> iter = ob.listPropertyRange("openingPeriods"); iter.hasNext();)
			System.out.println(iter.next());
		
		System.out.println("\nINSTANCES OF Restaurant");
		for(Iterator<String> iter = ob.listInstances("Restaurant"); iter.hasNext();)
			System.out.println(iter.next());
		
		System.out.println("\nVALUE OF Restaurant_1 --> openingPeriods");
		for(Iterator<String> iter = ob.listPropertyValue("Restaurant_1", "openingPeriods"); iter.hasNext();)
			System.out.println(iter.next());
		
		System.out.println("\nIs valid?");
		System.out.println(ob.isValid(false));
		
		System.out.println("\nValidation messages");
		for(Iterator<String> iter = ob.validate(); iter.hasNext();)
			System.out.println(iter.next());
		
		System.out.println("\nWhiteWine declared belonging clases");
		for(Iterator<String> iter = ob.listBelongingClasses("WhiteWine"); iter.hasNext();)
			System.out.println(iter.next());

		System.out.println("\nWhiteWine direct (not inferred) declared belonging clases");
		for(Iterator<String> iter = ob.listDeclaredBelongingClasses("WhiteWine"); iter.hasNext();)
			System.out.println(iter.next());

	}
}
