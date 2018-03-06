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
 * File: PnlConceptsAndInstancesTree.java
 * 26/02/2007
 */

package ucm.gaia.ontobridge.test.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;


import java.awt.*;
import java.io.File;

import org.apache.logging.log4j.LogManager;
import ucm.gaia.ontobridge.OntoBridge;

import java.util.*;


/**
 * Shows concepts, defined and inferred instances and saves the changes into a file
 * @author Juan Ant. Recio Garc�a
 */
public class PnlConceptsAndInstancesTree extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private JTree ontologyTree;
	private DefaultMutableTreeNode root;
	private JCheckBox inferredInstancesCB;
	private JButton update;
	private JButton save;
	private java.util.ArrayList<String> _instances = new java.util.ArrayList<String>();
	private OntoBridge ob;
	private boolean showsave;
	
	private static int maxdepth = 20; //Constant to avoid cycles;
	private static Icon CONCEPT  = new javax.swing.ImageIcon(PnlConceptsAndInstancesTree.class.getResource("/ucm/gaia/ontobridge/test/gui/class-orange.gif"));
	private static Icon INSTANCE = new javax.swing.ImageIcon(PnlConceptsAndInstancesTree.class.getResource("/ucm/gaia/ontobridge/test/gui/instance.gif"));
	
	/**
	 * Constructor
	 */
	public PnlConceptsAndInstancesTree(OntoBridge ob, boolean showsave) {
		super();
		this.showsave = showsave;
		createComponents();
		this.ob = ob;
		readOntology();
	}

	/**
	 * Constructor
	 */
	public PnlConceptsAndInstancesTree(OntoBridge ob, boolean inferenceEnabled, boolean showsave) {
		super();
		this.showsave = showsave;
		createComponents();
		this.inferredInstancesCB.setSelected(inferenceEnabled);
		this.ob = ob;
		readOntology();
	}
	
	protected void createComponents(){
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
		root= new DefaultMutableTreeNode("Thing");

		ontologyTree = new JTree(root);
		ontologyTree.setCellRenderer(new MyRenderer());
        ontologyTree.setSelectionModel(null);
        
        
		scrPnl = new JScrollPane(ontologyTree);
        scrPnl.setViewportView(ontologyTree);
		
		setLayout(new BorderLayout());
		add(scrPnl,BorderLayout.CENTER);
		
		JPanel options = new JPanel();
		inferredInstancesCB = new JCheckBox("Inferred Instances");
		update = new JButton("Update");
		save = new JButton("Save");
		options.setLayout(new BorderLayout());
		options.add(inferredInstancesCB, BorderLayout.NORTH);
		options.add(update, BorderLayout.CENTER);
		if(showsave)
			options.add(save, BorderLayout.SOUTH);
		add(options, BorderLayout.SOUTH);
		
		
		update.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				readOntology();	
			}
		});
		
		save.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				save();
			}
		});
	}

	
	protected void save()
	{
		javax.swing.JFileChooser jfc = new javax.swing.JFileChooser();
	    jfc.setFileFilter(new FileFilter(){
			public boolean accept(File f) {
				return f.getAbsolutePath().endsWith("owl");
			}
			public String getDescription() {
				return "OWL ontology RDF/XML";
			}
	    });
	    int returnVal = jfc.showSaveDialog(this);
	    if(returnVal == javax.swing.JFileChooser.APPROVE_OPTION) {
	    	String filename = jfc.getSelectedFile().getAbsolutePath();
	    	ob.save(filename);
	    }
	}
	
	/**
	 * Read the ontology classes.
	 * 
	 */
	protected void readOntology() {
		try 
		{
			root.removeAllChildren();
			Iterator<String> rc = ob.listRootClasses();
			while(rc.hasNext())
			{
				DefaultMutableTreeNode node = createNode(rc.next(), ob, 0);
				root.add(node);
			}
			ontologyTree.setModel(new DefaultTreeModel(root));
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
		
			Iterator<String> subClasses = ob.listSubClasses(nodeName, true);
			while(subClasses.hasNext())
			{
				String subClassName = ob.getShortName(subClasses.next());
				if(!subClassName.equals("owl:Nothing"))
					node.add(createNode(subClassName, ob, depth+1));
			}
			Iterator<String> instances;
			if(this.inferredInstancesCB.isSelected())
				instances = ob.listInstances(nodeName);
			else
				instances = ob.listDeclaredInstances(nodeName);
			
			while(instances.hasNext())
			{
				String instanceName = ob.getShortName(instances.next());
				node.add(new DefaultMutableTreeNode(instanceName));
				_instances.add(instanceName);
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
					if(_instances.contains( dmtn.getUserObject() ))
						setIcon(INSTANCE);
					else
						setIcon(CONCEPT);
				} catch (Exception e) {
					LogManager.getLogger().error(e);
				}
			
			    return this;
		}
	}
}



