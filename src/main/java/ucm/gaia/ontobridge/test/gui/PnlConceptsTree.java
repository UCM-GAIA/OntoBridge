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
 * File: PnlConceptsTree.java
 * 26/02/2007
 */
package ucm.gaia.ontobridge.test.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import java.awt.*;

import org.apache.logging.log4j.LogManager;
import ucm.gaia.ontobridge.OntoBridge;

import java.util.*;


/**
 * Shows the concepts hierarchy of an ontology
 * @author Juan Ant. Recio Garc�a
 *
 */
public class PnlConceptsTree extends JPanel  implements TreeSelectionListener{

	private static final long serialVersionUID = 1L;
	
	private JTree ontologyTree;
	private DefaultMutableTreeNode root;
	
	private static 	Icon CONCEPT = new javax.swing.ImageIcon(PnlConceptsTree.class.getResource("/ucm/gaia/ontobridge/test/gui/class-orange.gif"));

	
	private static int maxdepth = 20; //Constant to avoid cycles;
	      

	/**
	 * Constructor
	 */
	public PnlConceptsTree(OntoBridge ob) {
		super();
		createComponents();
		readOntology(ob);
	}

	
    public String getSelectedConcept(){
        return selectedConcept;
    }
    
    private String selectedConcept = null;
    
    public void valueChanged(TreeSelectionEvent event) {
        selectedConcept = ontologyTree.getLastSelectedPathComponent().toString();
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
        ontologyTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        ontologyTree.addTreeSelectionListener(this);
        
        ontologyTree.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                int selRow = ontologyTree.getRowForLocation(e.getX(), e.getY());
                TreePath selPath = ontologyTree.getPathForLocation(e.getX(), e.getY());
                if(selRow != -1) {
                        selectedConcept = selPath.toString();
                }
            }
        });
        
		scrPnl = new JScrollPane(ontologyTree);
        scrPnl.setViewportView(ontologyTree);
		
		setLayout(new BorderLayout());
		add(scrPnl,BorderLayout.CENTER);
	}
	
	/**
	 * Read the ontology classes.
	 * 
	 */
	protected void readOntology(OntoBridge ob) {
		try 
		{
			ontologyTree.getModel().getRoot();
			Iterator<String> rc = ob.listRootClasses();
			while(rc.hasNext())
			{
				DefaultMutableTreeNode node = createNode(rc.next(), ob, 0);
				root.add(node);
			}
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
					setIcon(CONCEPT);
				} catch (Exception e) {
					LogManager.getLogger().error(e);
				}
			
			    return this;
		}
	}
}



