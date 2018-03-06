/**
 * OntoBride library.
 * GAIA - Group for Artifical Intelligence Applications
 * Departamento de Ingenier�a del Software e Inteligencia Artificial
 * Universidad Complutense de Madrid
 * 
 * Licensed under the terms of the GNU Library or Lesser General Public License (LGPL)
 *
 * @author Juan A. Recio Garc�a
 * @version 1.2
 * 
 * This software is a subproject of the jCOLIBRI framework
 * http://sourceforge.net/projects/jcolibri-cbr/
 * http://gaia.fdi.ucm.es/projects/jcolibri/
 * 
 * File: SPARQL.java
 * Created by: Antonio A. S�nchez Ruiz-Granados
 * 22/03/2007
 */

package ucm.gaia.ontobridge;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Model;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * This class allows to ask SPARQL queries to the reasoner
 * @author Antonio A. S�nchez Ruiz-Granados
 */
public class SPARQL {
	
	private OntModel ONT_MODEL;
	
	public SPARQL(OntoBridge ob){
		ONT_MODEL = ob.getModel();
	}
	
	/**************************************************************/
	/*                SPARQL queries                              */
	/**************************************************************/

	/**
	 * Executes a SPARQL query of type ASK (boolean query). Returns true 
	 * if the query has any results and false if there are no matches.
	 */
	public boolean execAskQuery(String queryStr) {
		// Create a new query
		Query query = QueryFactory.create(queryStr);

		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, ONT_MODEL);
		boolean res = qe.execAsk();

		// Important - free up resources used running the query
		qe.close();
		
		return res;
	}
	
	/**
	 * Executes a SPARQL query of type SELECT and returns an iterator over 
	 * the solutions. This method creates a list in memory with all the 
	 * results so it must be use carefully. A query with a lot of results can 
	 * exhaust the memory.
	 * 
	 * To make a select query without taken care of memory, the next template 
	 * must be used:
	 * <pre>
	 * 		// Create a new query
	 * 		Query query = QueryFactory.create(queryStr);
	 *
	 *		// Execute the query and obtain results
	 *		QueryExecution qe = QueryExecutionFactory.create(query, ONT_MODEL);
	 *		ResultSet results = qe.execSelect();
	 *
	 *		// Use the results as needed...
	 *		QuerySolution sol;
	 *		while(results.hasNext()) {
	 *			sol = results.nextSolution();
	 *			...;
	 *		}
	 *
	 *		// Important - free up resources used running the query
	 *		qe.close();
	 * </pre>
	 */
	public Iterator<QuerySolution> execSelectQuery(String queryStr) {
		// Create a new query
		Query query = QueryFactory.create(queryStr);

		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, ONT_MODEL);
		ResultSet results = qe.execSelect();
		
		// Copy the results to a list.
		List<QuerySolution> res = new LinkedList<QuerySolution>();
		while(results.hasNext())
			res.add(results.nextSolution());

		// Important - free up resources used running the query
		qe.close();
		
		return res.iterator();
	}
	
	/**
	 * Executes a SPARQL query of type SELECT and prints the result as a table 
	 * in the specified stream (usually System.out)
	 */
	public void execSelectQueryAndPrint(String queryStr, java.io.PrintStream outStream) {
		// Create a new query
		Query query = QueryFactory.create(queryStr);

		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, ONT_MODEL);
		ResultSet results = qe.execSelect();
		
		// Output query results	
		ResultSetFormatter.out(outStream, results, query);

		// Important - free up resources used running the query
		qe.close();
	}

	/**
	 * Executes a SPARQL query of type CONSTRUCT and returns a new model 
	 * with the results.
	 */
	public Model execConstructQuery(String queryStr) {
		// Create a new query
		Query query = QueryFactory.create(queryStr);

		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, ONT_MODEL);
		Model model = qe.execConstruct();
		
		// Important - free up resources used running the query
		qe.close();
		
		return model;
	}
	
	/**
	 * Executes a SPARQL query of type DESCRIBE and returns a new model 
	 * with the results.
	 * 
	 * The DESCRIBE query returns a single result RDF graph containing RDF 
	 * data about resources. This data is not prescribed by a SPARQL query, 
	 * where the query client would need to know the structure of the RDF in 
	 * the data source, but, instead, is determined by the SPARQL query 
	 * processor. The query pattern is used to create a result set. 
	 * The DESCRIBE  form takes each of the resources identified in a solution, 
	 * together with any resources directly named by IRI, and assembles a single 
	 * RDF graph by taking a "description" from the target knowledge base. 
	 * The description is determined by the query service. The syntax DESCRIBE * 
	 * is an abbreviation that identifies all of the variables in a query.
	 */
	public Model execDescribeQuery(String queryStr) {
		// Create a new query
		Query query = QueryFactory.create(queryStr);

		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, ONT_MODEL);
		Model model = qe.execDescribe();
		
		// Important - free up resources used running the query
		qe.close();
		
		return model;
	}	
}
