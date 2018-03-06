/**
 * OntoBride library.
 * GAIA - Group for Artifical Intelligence Applications
 * Departamento de Ingeniería del Software e Inteligencia Artificial
 * Universidad Complutense de Madrid
 * 
 * Licensed under the terms of the GNU Library or Lesser General Public License (LGPL)
 *
 * @author Juan A. Recio García
 * @version 1.2
 * 
 * This software is a subproject of the jCOLIBRI framework
 * http://sourceforge.net/projects/jcolibri-cbr/
 * http://gaia.fdi.ucm.es/projects/jcolibri/
 * 
 * File: OntoBridge.java
 * 22/11/2006
 */
package ucm.gaia.ontobridge;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.reasoner.ValidityReport;
import com.hp.hpl.jena.util.iterator.Filter;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.ReasonerVocabulary;
import org.apache.logging.log4j.LogManager;
import org.mindswap.pellet.jena.PelletReasonerFactory;
import ucm.gaia.ontobridge.exceptions.NoLocalFileException;
import ucm.gaia.ontobridge.util.ToStringIterator;

import java.io.FileWriter;
import java.util.*;

/**
 * Main OntoBridge Class<br>
 * This class provides a simple wrapper for JENA.<br> 
 * It allows connecting to PELLET reasoner or any other that supports the DIG protocol.
 * Instead of using JENA classes this class returns strings containing the URIs
 * of the elements in the ontology.
 * Input parameters of the methods use URIs (or the qualified short representation) for
 * pointing to the ontology elements.<br>
 * Conversion between URIs and JENA objects is managed automatically
 * 
 * 
 * @author Juan A. Recio García
 * @version 1.5
 */
public class OntoBridge {

	/**
     * JENA Ontology Model
     */
	protected OntModel ONT_MODEL;

	/**
     * Base URI/Namespace for the main ontology
     */
	protected String BASE_NS;
	
	/** Backup variable used to store initial model when disabling inference */
	protected OntModel backupModel = null;

	/**
     * Creates an OntoBridge object.
     * Currentlly it does nothing.
     */
	public OntoBridge(){}
	
	
	public void credits() {
		String c = "\nThis is OntoBridge v1.5" +
				"\nDeveloped by GAIA - Group for Artificial Intelligence Applications." +
				"\nhttp://gaia.fdi.ucm.es";
		LogManager.getLogger().info(c);
	}
	
	/**
	 * Inits the OntoBridge with the PELLET Reasoner
	 */
	public void initWithPelletReasoner() {
		credits();
		ONT_MODEL = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC);
	}
	
	/**
	 * Inits the OntoBridge with a DIG reasoner.
	 * Dig is a protocol (working over http) that allows conecting with any reasoner.
	 * @param reasonerHost Host where the reasoner is running.
	 * @param reasonerPort Port that the reasoner listens to.
	 */
	/*public void initWithDIGReasoner(String reasonerHost, int reasonerPort)
	{
		credits();
		//Create a default model
		Model cModel = ModelFactory.createDefaultModel();

		//Obtain the reasonerURL
		String reasonerURL = "http://" + reasonerHost + ":" + reasonerPort;

		//Create a reasoner configuration with the reasoner URL
		Resource conf = cModel.createResource();
		conf.addProperty(ReasonerVocabulary.EXT_REASONER_URL, cModel.createResource(reasonerURL));

		//Obtain the Dig reasoners Factory
		DIGReasonerFactory drf = (DIGReasonerFactory) ReasonerRegistry
				.theRegistry().getFactory(DIGReasonerFactory.URI);

		//Obtain a reasoner with our configuration
		DIGReasoner r = (DIGReasoner) drf.create(conf);

		//Choose the Ontology Model Specification: OWL-DL in Memory
		OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_DL_MEM);
		spec.setReasoner(r);

		//Obtain the OntologyModel
		ONT_MODEL = ModelFactory.createOntologyModel(spec);
	}*/

	/**
	 * Inits the OntoBridge without reasoner.
	 * That way, this class doesn't use any inferred knowledge of the ontology
	 */
	public void initWithOutReasoner()
	{
		credits();
		ONT_MODEL = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
	}
	
	
	/**
	 * Enables or disables the inference. This method should be only used for performance reasons. 
	 * When disabling inference, current model is validated, backed-up and finally a new model is created without attached reasoner.
	 * If inference is enabled again the back-up model is restored.
	 * This method does not distinguish if the object was initializated without reasoner. If so, this method is unusefull.
	 * Any change performed in the ontology when inference is disabled will not be maintained when inference is enabled again, 
	 * because this method creates a copy of the current model.
	 * @param enabled Indicates if inference is enabled
	 */
	public void setInference(boolean enabled)
	{
		if(enabled)
		{
			if(backupModel != null)
			{
				ONT_MODEL = backupModel;
				backupModel = null;
			}
			else
				LogManager.getLogger().warn("Inference already enabled.");
		}
		else
		{
			if(backupModel == null)
			{
				//Validate the model (classify instances)
				ONT_MODEL.validate();
				//Backup model
				backupModel = ONT_MODEL;
				//Create a plain model with asserted and infered information (a-box + t-box)
				Model plain = ModelFactory.createModelForGraph( ONT_MODEL.getGraph() );
				//Create a new model without reasoner
				ONT_MODEL = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM);
				//Copy plain model into the model without reasoner
				ONT_MODEL.add(plain);
				//Copy ns prefixes
				ONT_MODEL.setNsPrefixes(backupModel.getNsPrefixMap());
			}
			else
				LogManager.getLogger().warn("Inference already disabled.");
		}
	}
	
	/**
	 * Loads an ontology using the initialized reasoner
	 * @param mainOntology Main ontology document
	 * @param subOntologies Collection of ontology documents used by the main ontology
	 * @param loadSubOntologies Indicates if the reasoner must load the subOntologies. 
	 *        If loaded, their content is included in the reasoning graph of the main ontology graph.
	 */
	public void loadOntology(OntologyDocument mainOntology, Collection<OntologyDocument> subOntologies, boolean loadSubOntologies)
	{
		//Configure local copies of the ontologies
		OntDocumentManager dm = ONT_MODEL.getDocumentManager();
		
		try {
			if(mainOntology.hasAltLocalFile())
				dm.addAltEntry(mainOntology.getURL(), mainOntology.getLocalfile());
		} catch (NoLocalFileException e) {
			LogManager.getLogger().error(e);
		}
		
		for(OntologyDocument od : subOntologies)
		{
			try {
			if(od.hasAltLocalFile())
				dm.addAltEntry(od.getURL(), od.getLocalfile());
			} catch (NoLocalFileException e) {
				LogManager.getLogger().error(e);
			}
		}
		
		LogManager.getLogger().info("Loading Main Ontology: "+mainOntology.getURL());
		ONT_MODEL.read(mainOntology.getURL());	
		BASE_NS = (String)ONT_MODEL.getNsPrefixMap().get("");
		
		if(loadSubOntologies)
			for(OntologyDocument od : subOntologies)
			{
				LogManager.getLogger().info("Loading Sub-Ontology: "+od.getURL());
				ONT_MODEL.read(od.getURL());
			}
		
		
		LogManager.getLogger().info("Loading Complete");
		//Print Namespaces
		LogManager.getLogger().info( "Base Namespace: "+ BASE_NS );
		LogManager.getLogger().info( "Namespaces loaded: " + ONT_MODEL.getNsPrefixMap() );
		
		preCalculateProfs();
		
	}

	/**************************************************************/
	/****************      Depth methods   ************************/
	/**************************************************************/

	protected HashMap<String, Integer> profs = new HashMap<String,Integer>();
	protected int maxDepth = 0;
	
	protected void preCalculateProfs()
	{
		profs.clear();
		
		String thing = getThingURI();
		profs.put(thing, new Integer(0));
		
		HashSet<String> subclasses = new HashSet<String>();
		
		Iterator<String> iter = this.listRootClasses();
		boolean cont = true;
		int i = 1;
		while(cont)
		{
			subclasses  = new HashSet<String>();
			while(iter.hasNext())
			{
				String _class = iter.next();
				profs.put(_class, new Integer(i));
					
				Iterator<String> aux = this.listSubClasses(_class,true);
				while(aux.hasNext())
					subclasses.add(aux.next());	
			}
			cont = !subclasses.isEmpty();
			iter = subclasses.iterator();
			i++;
		}
		maxDepth = i-2;
	}
	
	public int profConcept(String _class)
	{
		if(!_class.contains("#"))
			_class = getURI(_class);
		return profs.get(_class);
	}
	public int profInstance(String instance)
	{
		int res = Integer.MAX_VALUE;
		for(Iterator<String> iter = listDeclaredBelongingClasses(instance); iter.hasNext();)
		{
			int prof = profConcept(iter.next());
			if(prof<res)
				res = prof;
		}
		return res+1;
	}
	
	public int getMaxProf()
	{
		return maxDepth;
	}
	
	
	/**
	 * Returns the maximum profundity of the Least Common Subsumers of two instances.
	 */
	public int maxProfLCS(String instance1, String instance2)
	{
		Set<String> parents1 = new HashSet<String>();	
		for(Iterator<String> iter = listBelongingClasses(instance1);iter.hasNext(); )
			parents1.add(iter.next());
		

		Set<String> parents2 = new HashSet<String>();	
		for(Iterator<String> iter = listBelongingClasses(instance2);iter.hasNext(); )
			parents2.add(iter.next());	

		int maxProf = Integer.MIN_VALUE;
		
		parents1.retainAll(parents2);
		for(String c: parents1)
		{
			int pc = this.profConcept(c);
			if(pc>maxProf)
				maxProf = pc;
		}
		return maxProf;
	}
	
	/**
	 * Returns a set with the Least Common Subsumers of two instances. 
	 */
	public Set<String> LCS(String instance1, String instance2)
	{
		Set<String> parents1 = new HashSet<String>();	
		for(Iterator<String> iter = listBelongingClasses(instance1);iter.hasNext(); )
			parents1.add(iter.next());
		

		Set<String> parents2 = new HashSet<String>();	
		for(Iterator<String> iter = listBelongingClasses(instance2);iter.hasNext(); )
			parents2.add(iter.next());	
		
		Set<String> res = new HashSet<String>();
		int maxProf = Integer.MIN_VALUE;
		
		parents1.retainAll(parents2);
		for(String c: parents1)
		{
			int pc = this.profConcept(c);
			if(pc>maxProf)
			{
				res.clear();
				res.add(c);
				maxProf = pc;
			}
			else if(pc==maxProf)
			{
				res.add(c);
				maxProf = pc;
			}
			
		}
		return res;
	}
	
	/**************************************************************/
	/*      Functions for listing Classes of the Ontology         */
	/**************************************************************/
	
	/**
	 * Lists all the no anonymous classes of the ontology
	 */
	public Iterator<String> listAllClasses()
	{
		return new ToStringIterator<String>(ONT_MODEL.listClasses().filterDrop(new Filter() {
            public boolean accept( Object o ) {
                return ((Resource) o).isAnon();
            }} )
        );
	}

	/**
	 * Lists all the classes of the ontology including anonymous classes
	 */
	public Iterator<String> listAllandAnonClasses()
	{
		return new ToStringIterator<String>(ONT_MODEL.listClasses());
	}
	
	/**
	 * Lists the enumerated classes of the ontology
	 */
	public Iterator<String> listEnumeratedClasses()
	{
		return new ToStringIterator<String>(ONT_MODEL.listEnumeratedClasses().filterDrop( new Filter() {
            public boolean accept( Object o ) {
                return ((Resource) o).isAnon();
            }} )
        );
	}
	
	/**
	 * Lists the union classes of the ontology
	 */
	public Iterator<String> listUnionClasses()
	{
		return new ToStringIterator<String>(ONT_MODEL.listUnionClasses().filterDrop( new Filter() {
            public boolean accept( Object o ) {
                return ((Resource) o).isAnon();
            }} )
        );
	}
	
	/**
	 * Lists the complement classes of the ontology
	 */
	public Iterator<String> listComplementClasses()
	{
		return new ToStringIterator<String>(ONT_MODEL.listComplementClasses().filterDrop( new Filter() {
            public boolean accept( Object o ) {
                return ((Resource) o).isAnon();
            }} )
        );
	}
	
	/**
	 * Lists the intersection classes of the ontology
	 */
	public Iterator<String> listIntersectionClasses()
	{
		return new ToStringIterator<String>(ONT_MODEL.listIntersectionClasses().filterDrop( new Filter() {
            public boolean accept( Object o ) {
                return ((Resource) o).isAnon();
            }} )
        );
	}
	
	/**
	 * Lists the restrictions of the ontology
	 */
	public Iterator<String> listRestrictions()
	{
		return new ToStringIterator<String>(ONT_MODEL.listRestrictions().filterDrop( new Filter() {
            public boolean accept( Object o ) {
                return ((Resource) o).isAnon();
            }} )
        );
	}
	
	/**
	 * Lists the hierarchy root classes of the ontology
	 */
	public Iterator<String> listRootClasses()
	{
		return new ToStringIterator<String>(ONT_MODEL.listHierarchyRootClasses().filterDrop( new Filter() {
            public boolean accept( Object o ) {
                return ((Resource) o).isAnon();
            }} )
        );		
	}
	
	/**************************************************************/
	/*      Functions for checking sub-super classes and          */
	/*      instances belonging                                   */
	/**************************************************************/
	
	/**
	 * Lists the subclasses of a class. 
	 * @param ontClass class parent of the classes user is asking for
	 * @param direct return only direct subclasses
	 */
	public Iterator<String> listSubClasses(String ontClass, boolean direct)
	{
		try{
			OntClass ontC = obtainOntClass(ontClass);
			return new ToStringIterator<String>(ontC.listSubClasses(direct));
		}catch(Exception e)
		{
			return new ArrayList<String>().iterator();
		}
	}
	
	/**
	 * Lists the superclasses (parents) of a class. 
	 * @param ontClass subclass of the classes user is asking for
	 * @param direct return only direct subclasses
	 */
	public Iterator<String> listSuperClasses(String ontClass, boolean direct)
	{
		try
		{
			OntClass ontC = obtainOntClass(ontClass);
			return new ToStringIterator<String>(ontC.listSuperClasses(direct));
		}catch(Exception e)
		{
			return new ArrayList<String>().iterator();
		}
	}
	
	/**
	 * List the instances of a class
	 * @param ontClass class that instances belong to
	 */
	public Iterator<String> listInstances(String ontClass)
	{
		OntClass ontC = obtainOntClass(ontClass);
		return new ToStringIterator<String>(ontC.listInstances());
	}

	/**
	 * List the declared (not inferred) instances of a class
	 * @param ontClass class that instances belong to
	 */
	public Iterator<String> listDeclaredInstances(String ontClass)
	{
		ArrayList<String> list = new ArrayList<String>();
		OntClass ontC = obtainOntClass(ontClass);
		StmtIterator si = ONT_MODEL.getRawModel().listStatements(null, RDF.type, ontC);
		while(si.hasNext())
		{
			Statement st = si.nextStatement();
			list.add(st.getSubject().toString());
		}
		return list.iterator();
	}
	
	
	/**
	 * Lists the classes that an instance belongs to
	 * @param instance instance asked for belonging classes
	 */
	public Iterator<String> listBelongingClasses(String instance)
	{
		ArrayList<String> list = new ArrayList<String>();
		OntResource ontI = obtainOntResource(instance);
		StmtIterator si = ONT_MODEL.listStatements(ontI, RDF.type, (RDFNode)null);
		while(si.hasNext())
		{
			Statement st = si.nextStatement();
			list.add(st.getObject().toString());
		}
		return list.iterator();
		
	}
	
	/**
	 * Lists the declared (not inferred) classes that an instance belongs to
	 * @param instance instance asked for belonging classes
	 */
	public Iterator<String> listDeclaredBelongingClasses(String instance)
	{
		ArrayList<String> list = new ArrayList<String>();
		OntResource ontI = obtainOntResource(instance);
		StmtIterator si = ONT_MODEL.getRawModel().listStatements(ontI, RDF.type, (RDFNode)null);
		while(si.hasNext())
		{
			Statement st = si.nextStatement();
			list.add(st.getObject().toString());
		}
		return list.iterator();
		
	}
	
	/**
	 * Checks if a class is subclass of other
	 * @param subclass subclasss
	 * @param superclass superclass
	 */
	public boolean isSubClassOf(String subclass, String superclass)
	{
		OntClass subC = obtainOntClass(subclass);
		OntClass superC = obtainOntClass(superclass);
		StmtIterator si = ONT_MODEL.listStatements(subC, RDFS.subClassOf,superC);
		return si.hasNext();
	}

	/**
	 * Checks if an instance belongs to a class
	 * @param instance instance
	 * @param ontclass class
	 */
	public boolean isInstanceOf(String instance, String ontclass)
	{
		OntClass ontC = obtainOntClass(ontclass);
		OntResource ontI = obtainOntResource(instance);
		StmtIterator si = ONT_MODEL.listStatements(ontI, RDF.type, ontC);
		return si.hasNext();
	}
	
	/**
	 * Checks if a proporty is an OntologyProperty instead of a DataTypeProperty
	 * @param property
	 * @return if it is an OntologyProperty
	 */
	public boolean isOntoProperty(String property)
	{
		return ! (obtainOntProperty(property) instanceof DatatypeProperty);
	}
	
	
	/**************************************************************/
	/*      Functions for managing properties between             */
	/*      classes and instances                                 */
	/**************************************************************/
	
	/**
	 * Lists the properties applicable to a class
	 * @param ontClass class in the domain of the listed properties
	 */
	public Iterator<String> listProperties(String ontClass)
	{
		OntClass ontR = obtainOntClass(ontClass);
		return new ToStringIterator<String>(ontR.listDeclaredProperties());
	}
	
	/**
	 * Lists the specific properties of class. This method only returns the 
	 * properties that has the specified class in the domain
	 * @param ontClass class in the domain of the listed properties
	 */
	public Iterator<String> listSpecificProperties(String ontClass)
	{
		ArrayList<String> list = new ArrayList<String>();
		OntClass ontR = obtainOntClass(ontClass);
		for(Iterator iter = ontR.listDeclaredProperties(); iter.hasNext();)
		{
			OntProperty prop = (OntProperty)iter.next();
			StmtIterator si = ONT_MODEL.listStatements(prop, RDFS.domain, ontR);
			if(si.hasNext())
				list.add(prop.getURI());
		}
		return list.iterator();
	}
	
	/**
	 * Lists the classes on the range of a property
	 * @param property property user is asking for its range
	 */
	public Iterator<String> listPropertyRange(String property)
	{
		OntProperty prop = obtainOntProperty(property);
		return new ToStringIterator<String>(prop.listRange());
	}
	
	/**
	 * Lists the instances that are the values of a property of an instance.
	 * @param instance instance origin of the property
	 * @param property property 
	 */
	public Iterator<String> listPropertyValue(String instance, String property)
	{
		OntResource ontI = obtainOntResource(instance);
		Property prop = obtainOntProperty(property);
		return new ToStringIterator<String>(ontI.listPropertyValues(prop));
	}
	
	/**
	 * Lists the properties of an instance
	 * @param instance source of the properties
	 */
	public Iterator<String> listInstanceProperties(String instance)
	{
		ArrayList<String> list = new ArrayList<String>();
		OntResource ontI = obtainOntResource(instance);
		for(StmtIterator props = ontI.listProperties(); props.hasNext();)
			list.add(props.nextStatement().getPredicate().toString());
		return list.iterator();
	}
	
	/**
	 * Returns the properties with their corresponding values of an instance. The properties and values parameter are cleared.
	 * They are used to return the properties and values. property[i] has value values[i].
	 * @param instance source of the properties
	 * @param properties Array containing the name of the properties
	 * @param values Array containing the value of the property
	 */
	public void listInstancePropertiesValues(String instance, List<String> properties, List<String> values)
	{
		properties.clear();
		values.clear();
		
		
		OntResource ontI = obtainOntResource(instance);
		for(StmtIterator props = ontI.listProperties(); props.hasNext();)
		{
			Statement stmt = props.nextStatement();
			if(getShortName(stmt.getPredicate().toString()).equals("rdf:type"))
				continue;
			properties.add(stmt.getPredicate().toString());
			values.add(stmt.getObject().toString());	
		}
	}
	
	/**************************************************************/
	/*             Names and URIs management                      */
	/**************************************************************/
	
	/**
	 * Returns the short form of an URI.
	 */
	public String getShortName(String URI)
	{
		String sname = ONT_MODEL.shortForm(URI);
		if(sname.startsWith(":"))
			return sname.substring(1);
		else
			return sname;
	}
	
	/**
	 * Returns the long URI form of a short name
	 */
	public String getURI(String shortName)
	{
		String qname = ONT_MODEL.expandPrefix(shortName);
		if( ONT_MODEL.getOntResource(qname) != null)
			return qname;
		else
		    return BASE_NS + shortName;
	}
	
	public String getThingURI()
	{
		return ONT_MODEL.expandPrefix("owl:Thing");
	}
	
	/**************************************************************/
	/*              Existence Functions                           */
	/**************************************************************/
	
	/**
	 * Returns if a class exists
	 */
	public boolean existsClass(String name)
	{
		return obtainOntClass(name) != null;
	}
	
	/**
	 * Returns if a property exists
	 */
	public boolean existsProperty(String name)
	{
		return obtainOntProperty(name) != null;
	}
	
	/**
	 * Returns if an instance exists
	 */
	public boolean existsInstance(String name)
	{
		return obtainOntResource(name) != null;
	}

	/**
	 * Returns if an instance of a concrete class exists
	 */
	public boolean existsInstance(String instanceName, String className)
	{
		if(!existsInstance(instanceName))
			return false;
		return isInstanceOf(instanceName, className);
		
	}
	
	/**************************************************************/
	/*                Insertion functions                         */
	/**************************************************************/	
	
	/**
	 * Create a new class in the ontology. The className must not exist.
	 * @param className Class to create
	 */
	public void createClass(String className)
	{
		String longName;
		if(className.contains("#"))
			longName = className;
		if(className.contains(":"))
			longName= ONT_MODEL.expandPrefix(className);
		else
			longName = BASE_NS + className;
		
		ONT_MODEL.createClass(longName);
	}
	
	/**
	 * Sets a class as subclass of other.
	 * @param subClass
	 * @param superClass
	 */
	public void setSubClass(String subClass, String superClass)
	{
		OntClass _sub   = obtainOntClass(subClass);
		OntClass _super = obtainOntClass(superClass);
		
		_sub.setSuperClass(_super);
	}
	
	/**
	 * Creates a new instance of a class. The class must exist but the instance must not.
	 * @param className
	 * @param instanceName
	 */
	public void createInstance(String className, String instanceName)
	{
		
		OntClass c = obtainOntClass(className);
		
		String longName;
		if(instanceName.contains("#"))
			longName = instanceName;
		if(instanceName.contains(":"))
			longName= ONT_MODEL.expandPrefix(instanceName);
		else
			longName = BASE_NS + instanceName;
		
		c.createIndividual(longName);
	}
	
	/**
	 * Creates an ontology property between two instances. An ontology property ranges over instances.
	 * If there is other property with the same name this method creates a new one. 
	 * If you want to modify an existing property use the modifyOntProperty() method.
	 * @param sourceInstance
	 * @param propertyName
	 * @param destInstance
	 */
	public void createOntProperty(String sourceInstance, String propertyName, String destInstance)
	{
		OntResource si = this.obtainOntResource(sourceInstance);
		OntResource di = this.obtainOntResource(destInstance);
		Property  prop = this.obtainOntProperty(propertyName); 
		si.addProperty(prop, di);
	}
	
	/**
	 * Modifies the range of an ontology property. If the property does not exist it creates a new one.
	 * An ontology property ranges over instances.
	 * @param sourceInstance
	 * @param propertyName
	 * @param destInstance
	 */
	public void modifyOntProperty(String sourceInstance, String propertyName, String destInstance)
	{
		OntResource si = this.obtainOntResource(sourceInstance);
		OntResource di = this.obtainOntResource(destInstance);
		Property  prop = this.obtainOntProperty(propertyName); 
		si.setPropertyValue(prop, di);
	}
	
	/**
	 * Creates an dataType property between two instances. An ontology property ranges over XML-Schema datatypes.
	 * The method tries to find the proper datatype for the value object. (If this fails use the explicit datatyped method).
	 * @param sourceInstance
	 * @param propertyName
	 * @param value The value of the property. The method tries to find the proper datatype for this object.
	 */
	public void createDataTypeProperty(String sourceInstance, String propertyName, Object value)
	{
		OntResource si = this.obtainOntResource(sourceInstance);
		Property  prop = this.obtainOntProperty(propertyName);
		si.addProperty(prop, ONT_MODEL.createTypedLiteral(value)); 	
	}
	
	/**
	 * Creates an dataType property between two instances. An ontology property ranges over XML-Schema datatypes.
	 * The datatype of the value is explicitely defined.
	 * @param sourceInstance
	 * @param propertyName
	 * @param value The value of the property.
	 * @param valueDataType DataType of the value
	 */
	public void createDataTypeProperty(String sourceInstance, String propertyName, String value, String valueDataType)
	{
		OntResource si = this.obtainOntResource(sourceInstance);
		Property  prop = this.obtainOntProperty(propertyName);
		si.addProperty(prop, ONT_MODEL.createTypedLiteral(value, valueDataType)); 	
	}
	
	/**************************************************************/
	/*                 Delete Functions                          */
	/**************************************************************/
	
	/**
	 * Deletes a class or instance from the model, including all of the properties that have this resource as domain or range.
	 * @param name resource to delete
	 */
	public void delete(String name)
	{
		OntResource res = this.obtainOntResource(name);
		res.remove();
	}
	
	
	/**
	 * Deletes all properties of an instance. <br>
	 * This method deletes every property with the given name.<br>
	 * Properties can be DataType or Ontological.
	 * <br>Thanks to: Carlos Rodriguez Fernandez<br> 
	 * @param sourceInstance intance that contains the properties
	 * @param property to delete
	 */
	public void deleteProperties(String sourceInstance, String property)
	{
		OntResource si = this.obtainOntResource(sourceInstance);
		Property prop = this.obtainOntProperty(property);
		si.removeAll(prop);
	}
	
	/**
	 * Deletes an ontological property that relates two instances.
	 * <br>Thanks to: Carlos Rodriguez Fernandez<br>
	 * @param sourceInstance 
	 * @param property to delete
	 * @param destInstance
	 * 
	 */
	public void deleteOntProperty(String sourceInstance, String property, String destInstance)
	{
		OntResource si = this.obtainOntResource(sourceInstance);
		OntResource di = this.obtainOntResource(destInstance);
		Property prop = this.obtainOntProperty(property);	
		si.removeProperty(prop, di);
	}
	
	/**************************************************************/
	/*                Persistence Functions                       */
	/**************************************************************/
	
	/**
	 * Saves the ontology (without imported ontologies data) into a file.
	 * @param fileName file path for saving the ontology
	 */
	public void save(String fileName)
	{
		try {
			FileWriter fw = new FileWriter(fileName);
			save(fw);
		} catch (Exception e) {
			LogManager.getLogger().error(e);
		}
	}
	
	public void save(FileWriter fw)
	{
		try {
			ONT_MODEL.write(fw,"RDF/XML-ABBREV");
			LogManager.getLogger().info( "Saving ontology.");
		} catch (Exception e) {
			LogManager.getLogger().error(e);
		}
		
	}
	
	
	/**************************************************************/
	/*               Ontology Validation Methods                  */
	/**************************************************************/

	/**
	 * Computes if the current ontology is valid. (Created by: Antonio Sanchez)
	 * @param ignorewarnings This param indicates if the method must return true if warnings are found
	 */
	public boolean isValid(boolean ignorewarnings) {
		ValidityReport vr = ONT_MODEL.validate();
		if(ignorewarnings)
			return vr.isValid();
		else
			return vr.isClean();
	}
	
	/**
	 * Computes if the current ontology is valid and returns a list of warnings and errors. (Created by: Antonio Sanchez)
	 */
	public Iterator<String> validate() {
		ValidityReport vr = ONT_MODEL.validate();
		
		Collection<String> reports = new ArrayList<String>();
		
		for(Iterator riter = vr.getReports(); riter.hasNext();)
		{
			ValidityReport.Report r = (ValidityReport.Report)riter.next();
			String msg ="";
			if(r.isError())
				msg += "[ERROR]";
			else
				msg += "[WARNING]";
			msg+="["+r.getType()+"]";
			msg+=r.getDescription();
			reports.add(msg);
		}
		
		return reports.iterator();
	}
	
	/**************************************************************/
	/*                  Extension Method                          */
	/**************************************************************/
	
	/**
	 * This method allows extensions using the
	 * Ontology Model used internally by OntoBridge
	 */
	public OntModel getModel()
	{
		return this.ONT_MODEL;
	}
	
	
	/**************************************************************/
	/*                Private functions                           */
	/**************************************************************/
	
	/**
	 * Returns the JENA OntClass referenced by the string.
	 * This method checks if the string is a short or long URI.
	 */
	protected OntClass obtainOntClass(String ontClass)
	{
		OntResource ontR = obtainOntResource(ontClass);
		if(ontR != null)
			if(ontR.canAs(OntClass.class))
				return ontR.asClass();
		return null;	
	}
	
	
	/**
	 * Returns the JENA OntProperty referenced by the string.
	 * This method checks if the string is a short or long URI.
	 */
	protected OntProperty obtainOntProperty(String ontProperty)
	{
		OntResource ontR = obtainOntResource(ontProperty);
		if(ontR != null)
			if(ontR.isProperty())
			{
				if(ontR.isDatatypeProperty())
					return ontR.asDatatypeProperty();
				return ontR.asProperty();
			}
		return null;	
	}
	
	/**
	 * Returns the JENA OntResource referenced by the string.
	 * This method checks if the string is a short or long URI.
	 */
	protected OntResource obtainOntResource(String ontRes)
	{
		OntResource ontR;
		try {
			ontR = ONT_MODEL.getOntResource(ontRes);
			if(ontR != null)
				return ontR;
		} catch (Exception e) {
		}
		
		try {
			String qname = ONT_MODEL.expandPrefix(ontRes);
			ontR = ONT_MODEL.getOntResource(qname);
			if(ontR != null)
				return ontR;
		} catch (Exception e) {
		}	
		
		try {
			return ontR = ONT_MODEL.getOntResource(BASE_NS + ontRes);
		} catch (Exception e) {
		}
		
		LogManager.getLogger().error("Ontology Resource not found: "+ ontRes);
		return null;
	}
	
}
