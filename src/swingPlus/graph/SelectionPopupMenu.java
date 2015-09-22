package swingPlus.graph;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.Border;

import org.apache.log4j.Logger;


import model.graph.Edge;
import model.graph.GraphModel;

import util.DataSpecificAction;
import util.GraphicsUtil;
import util.Messages;



public class SelectionPopupMenu extends JPopupMenu {

    /**
	 * 
	 */
	private static final long serialVersionUID = 5412424964175073092L;
	
	private static final Logger LOGGER = Logger.getLogger (SelectionPopupMenu.class);
    private static final Border EMPTY_BORDER = BorderFactory.createEmptyBorder (1, 12, 1 ,2);
    private static final Border MATTE_BORDER = BorderFactory.createMatteBorder (1, 0, 1 ,0, Color.gray);
    protected static final Border LABEL_BORDER = BorderFactory.createCompoundBorder (EMPTY_BORDER, MATTE_BORDER);

    
    private static final SelectionPopupMenu INSTANCE = new SelectionPopupMenu ();
	
    protected Object obj;
    Point mousePoint = new Point (0, 0);
    protected JLabel specificActionLabel, title, relationLabel;
    protected JMenuItem all;
    EncompassAllAction encompassAllAction;
    static final Font TYPEFACE = Font.decode (Messages.getString (GraphicsUtil.GRAPHICPROPS, "popupMenu.font"));
    static final Font TITLETYPEFACE = Font.decode (Messages.getString (GraphicsUtil.GRAPHICPROPS, "popupMenu.titlefont"));
  
    List<DataSpecificAction> dataSpecificActs = new ArrayList<DataSpecificAction> ();

    
    
    protected SelectionPopupMenu () {
  		
    	super ();
    	
        setForeground (Color.decode (Messages.getString (GraphicsUtil.GRAPHICPROPS, "ParCoordUI.backgroundColour")));

        //this.setLayout(new BoxLayout (this, BoxLayout.Y_AXIS));
        
        this.addFocusListener(
    		new FocusAdapter () {
    			@Override
				public void focusLost (final FocusEvent fEvent) {
    				SelectionPopupMenu.this.setVisible(false);
    			}
    		}
        );        
        
        
		relationLabel = new JLabel ("Navigate to Relations");
		relationLabel.setBorder (LABEL_BORDER);
		relationLabel.setForeground (Color.blue);
		
		specificActionLabel = new JLabel ("Marker Actions");
		specificActionLabel.setBorder (LABEL_BORDER);
		specificActionLabel.setForeground (Color.blue);
		
        title = new JLabel ("Object");  
        title.setBorder (EMPTY_BORDER);
        title.setFont (TITLETYPEFACE);
        //title.setForeground (Color.);
		

        
        all = new JMenuItem ("View All");
        encompassAllAction = new EncompassAllAction ();
        all.addActionListener (encompassAllAction);    
	}


    protected Point getMousePoint () {
    	return this.getInvoker().getMousePosition();
    }
    
	public static SelectionPopupMenu getInstance() { return INSTANCE; }

	public void addDataSpecificAction (final DataSpecificAction dsAction) {
		dataSpecificActs.add (dsAction);
	}
	

	
    public void setNodeDetails (final JComponent jComp, final Point point, final Object obj) {     
    	if (jComp instanceof JGraph) {
    		final JGraph jgraph = (JGraph)jComp;
    		final GraphModel graph = jgraph.getFilteredModel ();
    		final Set<Edge> edges = graph.getEdges (obj);
    		this.obj = obj;
    		
    		this.removeAll();
    		
    		title.setText (obj.toString());
    		add (title);          
    		
    		addEdgeBasedMenuActions (jgraph, edges);
    		
    		mousePoint.setLocation (point);
    		
    		if (this.getComponentCount() > 2) {
    			encompassAllAction.setObject (obj);
    			this.add (all);
    			
    			if (!dataSpecificActs.isEmpty()) {
	    			this.add (specificActionLabel);
	    			for (DataSpecificAction dsAction : dataSpecificActs) {
	    				dsAction.setObject (obj);
	    				this.add (dsAction);
	    			}
    			}
   	
        		for (int i = 1; i < getComponents().length; i++) {
        			getComponent(i).setFont (TYPEFACE);
        		}
    			
    			this.show (jComp, point.x, point.y);
    		}
    	}
    }
    
    
    protected void addEdgeBasedMenuActions (final JGraph jgraph, final Set<Edge> edges) {
    	final Iterator<Edge> iEdge = edges.iterator();
    	boolean isolatedNode = false;
    	 
    	while (iEdge.hasNext ()) {    			
			final Edge edge = iEdge.next ();
			final Object node1 = edge.getNode1 ();
			final Object node2 = edge.getNode2 ();
			final Object otherObj = (obj.equals (node1) ? node2 : node1);

			final JMenuItem relation = new JMenuItem ();
			relation.setText (otherObj.toString());
			relation.setToolTipText ("tip "+otherObj.toString());
			
			if (!isolatedNode) {
				add (relationLabel);
				isolatedNode = true;
			}
			add (relation);
	        relation.addActionListener (
            	new ActionListener () {
        			public void actionPerformed (final ActionEvent aEvent) {
                        jgraph.moveTo (otherObj, getMousePoint());
                        SelectionPopupMenu.this.setVisible (false);
                    }	
            	}
            );
		}
    }
    
    
    class EncompassAllAction implements ActionListener {

    	Object obj;
    	
    	public void setObject (final Object obj) {
    		this.obj = obj;
    	}
    	
		@Override
		public void actionPerformed (final ActionEvent aEvent) {
			
			final JGraph graph = (JGraph)SelectionPopupMenu.this.getInvoker();
			final Set<Edge> edges = graph.getFilteredModel().getEdges (obj);
			
			if (!edges.isEmpty ()) {
				final Iterator<Edge> iEdge = edges.iterator();
				final Set<Object> objs = new HashSet<Object> ();
				while (iEdge.hasNext()) {
					final Edge edge = iEdge.next ();
					objs.add (edge.getNode1());
					objs.add (edge.getNode2());
				}
			
				graph.fitTo (objs);
				SelectionPopupMenu.this.setVisible (false);
			}
		}
    }
}