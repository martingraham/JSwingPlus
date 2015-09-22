package example.matrix;


import java.awt.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.plaf.metal.MetalLookAndFeel;

import example.matrix.renderers.ColourRenderer;

import model.graph.GraphModel;

import swingPlus.graph.force.impl.SimpleEdgeAttractor;
import util.ui.NewMetalTheme;


public class JMatrixDemo2 extends JMatrixDemo {
	
	/**
	 * @param args
	 */
	public static void main (final String[] args) {	
		//final MetalLookAndFeel lf = new MetalLookAndFeel();
		MetalLookAndFeel.setCurrentTheme (new NewMetalTheme());	
		new JMatrixDemo2 ();
	}

	public JMatrixDemo2 () {	
		super (null);
		table.setDefaultRenderer (Color.class, new ColourRenderer ());
		jgraph.setAttractiveForceCalculator (new SimpleEdgeAttractor ());
	}
	
	
	
	@Override
	public void addData (final GraphModel graph, final String zipFileName) {
		addNodes (graph);
		addRandomColours (graph);
	}
	

	
	protected void removeRandomNodes (final GraphModel model) {
		final List<Object> nodeList = new ArrayList<Object> (model.getNodes ());
		model.removeNode (nodeList.get(0));
		model.removeNode (nodeList.get(1));
	}
	
	
	protected final void addNodes (final GraphModel model) {
		for (int n = 0; n < 500; n++) {
			model.addNode (Integer.toString (n));
		}
	}

	
	
	protected final void addRandomColours (final GraphModel model) {
		final List<Object> nodeList = new ArrayList<Object> (model.getNodes ());
		final Color[] swatch = {Color.blue, Color.green, Color.red};
		final Random randObj = new Random ();
		
		for (int n = 0; n < 20000; n++) {
			final Object node1 = nodeList.get (randObj.nextInt (nodeList.size()));
			final Object node2 = nodeList.get (randObj.nextInt (nodeList.size()));
			final Color colour = swatch [randObj.nextInt (3)];
			model.addEdge (node1, node2, colour);
		}
	}
}
