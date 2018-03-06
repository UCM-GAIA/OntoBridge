/**
 * OntoBride library.
 * GAIA - Group for Artifical Intelligence Applications
 * Departamento de Ingenier�a del Software e Inteligencia Artificial
 * Universidad Complutense de Madrid
 * 
 * Licensed under the terms of the GNU Library or Lesser General Public License (LGPL)
 *
 * @author Juan A. Recio Garc�a
 * @version 1.0 beta
 * 
 * This software is a subproject of the jCOLIBRI framework
 * http://sourceforge.net/projects/jcolibri-cbr/
 * http://gaia.fdi.ucm.es/projects/jcolibri/
 * 
 * File: PnlInstancesTree.java
 * 26/02/2007
 */
package ucm.gaia.ontobridge.test.gui;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import java.awt.*;

import org.apache.logging.log4j.LogManager;
import ucm.gaia.ontobridge.OntoBridge;

import java.util.*;


/**
 * Shows the relationships of an instance
 * @author Juan Ant. Recio Garc�a
 *
 */
public class PnlInstancesTree extends JPanel{

	private static final long serialVersionUID = 1L;
	
	private JTree ontologyTree;
	private DefaultMutableTreeNode root;
	
	private static 	Icon INSTANCE = new javax.swing.ImageIcon(PnlInstancesTree.class.getResource("/ucm/gaia/ontobridge/test/gui/instance.gif"));
	private static 	Icon DATATYPE = new javax.swing.ImageIcon(PnlInstancesTree.class.getResource("/ucm/gaia/ontobridge/test/gui/datatype.gif"));
	private static 	Icon PROPERTY = new javax.swing.ImageIcon(PnlInstancesTree.class.getResource("/ucm/gaia/ontobridge/test/gui/property.gif"));

	
	private static int maxdepth = 20; //Constant to avoid cycles;
	private static ArrayList<String> drawnInstances = new ArrayList<String>(); //avoid cycles between instances
	private static Set<String> datatypes = new java.util.HashSet<String>();    

	/**
	 * Constructor
	 */
	public PnlInstancesTree(OntoBridge ob, String instance) {
		super();
		createComponents(instance);
		showInstance(ob, instance);
	}

	protected void createComponents(String instance){
		JScrollPane scrPnl;
		Border lineBorder, titleBorder, emptyBorder, compoundBorder;
		
		//set border and layout
		emptyBorder = BorderFactory.createEmptyBorder(0, 5, 0, 5);
		lineBorder = BorderFactory.createLineBorder(Color.BLACK);
		titleBorder = BorderFactory.createTitledBorder(lineBorder, "Ontology Structure");
		compoundBorder = BorderFactory.createCompoundBorder(titleBorder,
				emptyBorder);
		setBorder(compoundBorder);
		
		//set Ontology
		root= new DefaultMutableTreeNode(instance);

		ontologyTree = new JTree(root);
		ontologyTree.setCellRenderer(new MyRenderer());
        ontologyTree.setSelectionModel(null);
        
		scrPnl = new JScrollPane(ontologyTree);
        scrPnl.setViewportView(ontologyTree);
		
		setLayout(new BorderLayout());
		add(scrPnl,BorderLayout.CENTER);
	}
	
	/**
	 * Read the ontology classes.
	 * 
	 */
	protected void showInstance(OntoBridge ob, String instance) {
		try 
		{
			ontologyTree.setModel(new DefaultTreeModel(createNode(instance, ob, 0)));
	        ontologyTree.expandRow(0);
	        
		} catch (Exception e) {
			LogManager.getLogger().error(e);
		}
	}

	
	private DefaultMutableTreeNode createNode(String nodeName, OntoBridge ob, int depth)
	{
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(ob.getShortName(nodeName));
		if(depth > maxdepth)
			return node;
		if(drawnInstances.contains(ob.getShortName(nodeName)))
			return node;
		
		drawnInstances.add(ob.getShortName(nodeName));
		ArrayList<String> properties = new ArrayList<String>();
		ArrayList<String> values = new ArrayList<String>();
		ob.listInstancePropertiesValues(nodeName, properties, values);
		Iterator<String> propI = properties.iterator();
		Iterator<String>  valI = values.iterator();
		while(propI.hasNext())
		{
			String property = propI.next();
			String value = valI.next();
			DefaultMutableTreeNode propnode = new DefaultMutableTreeNode(ob.getShortName(property));
			node.add(propnode);
			if(ob.isOntoProperty(property))
				propnode.add(createNode(value, ob, depth+1));
			else
			{
				propnode.add(new DefaultMutableTreeNode(value));
				datatypes.add(value);
			}
		}

		return node;
	}


	class MyRenderer extends DefaultTreeCellRenderer {
		private static final long serialVersionUID = 1L;
		
		public MyRenderer() {
		}

		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean sel, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {

				super.getTreeCellRendererComponent(tree, value, sel, expanded,
												   leaf, row, hasFocus);
				
				try {
					DefaultMutableTreeNode dmtn = (DefaultMutableTreeNode)value;
					Object o = dmtn.getUserObject();
					if(datatypes.contains(o))
						setIcon(DATATYPE);
					else if(drawnInstances.contains(o))
						setIcon(INSTANCE);
					else
						setIcon(PROPERTY);
				} catch (Exception e) {
					LogManager.getLogger().error(e);
				}
			
			    return this;
		}
	}
	
}



