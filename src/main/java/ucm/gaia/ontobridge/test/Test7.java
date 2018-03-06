package ucm.gaia.ontobridge.test;

import java.util.ArrayList;

import com.hp.hpl.jena.rdf.model.Model;

import ucm.gaia.ontobridge.OntoBridge;
import ucm.gaia.ontobridge.OntologyDocument;
import ucm.gaia.ontobridge.SPARQL;

/**
 * File used for testing the SPARQL class using the restaurants ontology.
 * 
 * @author Antonio A. S�nchez Ruiz-Granados
 */
public class Test7 {

	public static void main(String args[]) {
		OntoBridge ob = new OntoBridge();
		ob.initWithPelletReasoner();

		OntologyDocument mainOnto = new OntologyDocument("http://gaia.fdi.ucm.es/ontologies/restaurant.owl","file:test/restaurant.owl");
		
		ArrayList<OntologyDocument> subOntologies = new ArrayList<OntologyDocument>();
		subOntologies.add(new OntologyDocument("http://gaia.fdi.ucm.es/ontologies/calendar.owl","file:test/calendar.owl"));
		subOntologies.add(new OntologyDocument("http://gaia.fdi.ucm.es/ontologies/price.owl","file:test/price.owl"));
		subOntologies.add(new OntologyDocument("http://gaia.fdi.ucm.es/ontologies/address.owl","file:test/address.owl"));
		subOntologies.add(new OntologyDocument("http://gaia.fdi.ucm.es/ontologies/contact-details.owl","file:test/contact-details.owl"));
		
		ob.loadOntology(mainOnto, subOntologies, false);
		
		SPARQL sparql = new SPARQL(ob);
						
		System.out.println();
		System.out.println("SPARQL SELECT query: subclasses of Cuisine");
		String query1 = 
			"PREFIX ro: <http://gaia.fdi.ucm.es/ontologies/restaurant.owl#> " +
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
			"SELECT DISTINCT ?a " +
			"WHERE { ?a rdfs:subClassOf ro:Cuisine } " + 
			"ORDER BY ?a";
		sparql.execSelectQueryAndPrint(query1, System.out);
		
		System.out.println();
		System.out.println("SPARQL ASQ query: are there any instances with name 'restaurant1'?");
		String query2 = 
			"PREFIX ro: <http://gaia.fdi.ucm.es/ontologies/restaurant.owl#> " +
			"ASK { ?a ro:name \"restaurant1\" }";
		System.out.println(sparql.execAskQuery(query2));
		
		System.out.println();
		System.out.println("SPARQL CONSTRUCT query: new model with the instances of Restaurant");
		String query3 = 
			"PREFIX ro: <http://gaia.fdi.ucm.es/ontologies/restaurant.owl#> " +
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"CONSTRUCT { ?a rdf:type ro:Cuisine } " +
			"WHERE { ?a rdf:type ro:Restaurant }";
		Model m3 = sparql.execConstructQuery(query3);
		m3.write(System.out, /*"N-TRIPLE"*/ "RDF/XML-ABBREV");
		
		System.out.println();
		System.out.println("SPARQL DESCRIBE query: new model with a description of the instances of Restaurant");
		String query4 = 
			"PREFIX ro: <http://gaia.fdi.ucm.es/ontologies/restaurant.owl#> " +
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"DESCRIBE ?a " +
			"WHERE { ?a rdf:type ro:Restaurant }";
		Model m4 = sparql.execDescribeQuery(query4);;
		m4.write(System.out, /*"N-TRIPLE"*/ "RDF/XML-ABBREV");		
	}
}
