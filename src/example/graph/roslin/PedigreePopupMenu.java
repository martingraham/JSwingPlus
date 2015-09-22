package example.graph.roslin;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;

import model.graph.Edge;

import swingPlus.graph.JGraph;
import swingPlus.graph.SelectionPopupMenu;


public class PedigreePopupMenu extends SelectionPopupMenu {

	/**
	 * 
	 */
	private static final long serialVersionUID = -327918537646850066L;
	private static final PedigreePopupMenu INSTANCE2 = new PedigreePopupMenu ();
	
    protected JMenuItem mum, dad;
    protected Object mumObj, dadObj;
    protected JLabel parentLabel, childrenLabel;
    
	public static PedigreePopupMenu getInstance() { return INSTANCE2; }
	
	
    protected PedigreePopupMenu () {
  		
        super ();     
            
        parentLabel = new JLabel ("Navigate to Parents");  
        parentLabel.setBorder (LABEL_BORDER);
        parentLabel.setForeground (Color.blue);
        
		childrenLabel = new JLabel ("Navigate to Children");
		childrenLabel.setBorder (LABEL_BORDER);
		childrenLabel.setForeground (Color.blue);

		
        mum = new JMenuItem ("Mum");
        dad = new JMenuItem ("Dad");
        
        
        final JMenuItem[] menuArray = {mum, dad};
        for (final JMenuItem jmi : menuArray) {
        	final String text = jmi.getText ();
        	jmi.setToolTipText (text+" tip");
        }
        
        mum.addActionListener (
        	new ActionListener () {
    			public void actionPerformed (final ActionEvent aEvent) {
    				final JGraph graph = (JGraph)PedigreePopupMenu.this.getInvoker();
                    graph.moveTo (mumObj, getMousePoint());
                    PedigreePopupMenu.this.setVisible (false);
                }	
        	}
        );

        dad.addActionListener (
        	new ActionListener () {
    			public void actionPerformed (final ActionEvent aEvent) {
    				final JGraph graph = (JGraph)PedigreePopupMenu.this.getInvoker();
    				graph.moveTo (dadObj, getMousePoint());
                    PedigreePopupMenu.this.setVisible (false);
                }	
        	}
        );    
	}
    
    
    
    @Override
    public void setNodeDetails (final JComponent jComp, final Point point, final Object obj) {     
       	if (jComp instanceof JGraph) {
    		dad.setVisible (false);
    		mum.setVisible (false);
    		
    		super.setNodeDetails (jComp, point, obj);
       	}
    }
    
    
    @Override
    protected void addEdgeBasedMenuActions (final JGraph jgraph, final Set<Edge> edges) {
    	boolean children = false;
    	 
    	for (Edge edge : edges) {   			
    		//final Object node1 = edge.getNode1 ();
    		final Object node2 = edge.getNode2 ();
    		
			if (node2 != obj) {
				final Object objChild = (Object) node2;
				final JMenuItem child = new JMenuItem ();
				child.setText (node2.toString());
				child.setToolTipText ("tip "+node2.toString());
				
				if (!children) {
					add (childrenLabel);
					children = true;
				}
				add (child);
		        child.addActionListener (
	            	new ActionListener () {
	        			public void actionPerformed (final ActionEvent aEvent) {
	                        jgraph.moveTo (objChild, getMousePoint());
	                        PedigreePopupMenu.this.setVisible (false);
	                    }	
	            	}
	            );
			}
		}
    	    	
    	add (parentLabel);
    	
    	for (Edge edge : edges) {   
    		final Object node1 = edge.getNode1 ();
    		final Object node2 = edge.getNode2 ();
    		
			if (node2 == obj && node1 instanceof Animal) {
				final Animal animal = (Animal)node1;
				
				if (animal.isMale()) {
					dadObj = animal;
					add (dad);
					dad.setText (animal.toString());
					dad.setVisible (true);
				} else {
					mumObj = animal;
					add (mum);
					mum.setText (animal.toString());
					mum.setVisible (true);
				}
			}
		}
    }

}
