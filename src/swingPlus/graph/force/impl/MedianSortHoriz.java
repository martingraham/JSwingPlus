package swingPlus.graph.force.impl;


import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingConstants;

import org.apache.log4j.Logger;

import model.graph.Edge;
import model.graph.GraphModel;

import swingPlus.graph.JGraph;
import swingPlus.graph.ObjectPlacement;
import swingPlus.graph.ObjectPlacementMapping;
import swingPlus.graph.force.AttractiveForceCalculationInterface;
import util.MathUtils;
import util.collections.ArrayListUtil;



public class MedianSortHoriz implements AttractiveForceCalculationInterface {

	private static final Logger LOGGER = Logger.getLogger (MedianSortHoriz.class);
	
	protected boolean firstTime = true;
	protected boolean listenerAdded = false;
	
	protected List<List<Object>> layerLists = new ArrayList<List<Object>> ();
	protected Map<Object, Integer> ranks = new HashMap<Object, Integer> ();
	protected List<Set<Edge>> edgeSets;
	protected List<List<Edge>> lowestEdgeLists;
	
	
	protected final Comparator<ObjectPlacement> xComp = new OPXComparator ();
	protected final Comparator<ObjectPlacement> yComp = new OPYComparator ();
	protected final Comparator<ObjectPlacement> orientedAxisComp;
	protected boolean orientation;	// true = horizontal;
	
	protected int layerSep, minObjSep;
	protected int maxLayerWidth;
	
	
	public MedianSortHoriz () {
		this (SwingConstants.HORIZONTAL);
	}
	
	public MedianSortHoriz (final int horizOrVert) {
		orientation = (horizOrVert == SwingConstants.HORIZONTAL);
		orientedAxisComp = (orientation ? xComp : yComp);
		layerSep = 320;
		minObjSep = 60;
	}
	
	
	@Override
	public void cleanup () {
		// EMPTY
	}
	
	
	@Override
	public void calculateAttractiveForces (final JGraph graph) {
		final GraphModel model = graph.getFilteredModel ();
		final ObjectPlacementMapping objMapping = graph.getObjectPlacementMapping();
		
		if (firstTime) {
			firstTime = false;
			allocateLayers (model); // layerLists and ranks set up in here
			startingLayout (model, objMapping, layerLists);
			edgeSets = makeEdgeSets (model, layerLists);
			lowestEdgeLists = makeLowestEdgeLists (model, layerLists, ranks);
		}
		
		if (!listenerAdded) {
			graph.addPropertyChangeListener (
				new PropertyChangeListener () {
					@Override
					public void propertyChange (final PropertyChangeEvent evt) {
						if ("graphFilterModel".equals (evt.getPropertyName())) {
							firstTime = true;
						}	
					}
				}
			);
			listenerAdded = true;
		}
		
		sort (model, objMapping, layerLists, ranks);
	}
	
	public void reset () {
		firstTime = true;
	}
	
	// Space between layers
	public void setLayerSep (final int layerSep) {
		this.layerSep = layerSep;
	}
	
	// Minimum space along layer axis per object
	public void setMinObjSep (final int minObjSep) {
		this.minObjSep = minObjSep;
	}
	
	
	
	
	public void allocateLayers (final GraphModel model) {
		final Set<Object> nodes = model.getNodes ();
		layerLists.clear ();
		ranks.clear ();
		final Set<Object> nodesToProcess = new HashSet<Object> ();
		final List<Object> nodesToProcessList = new ArrayList<Object> ();
		
		for (Object node : nodes) {
			final Set<Edge> edges = model.getEdges (node);
			
			boolean allFrom = true;
			for (Edge edge : edges) {
				if (edge.getNode2() == node) {
					allFrom = false;
					break;
				}
			}
			
			if (allFrom) {
				nodesToProcess.add (node);
				nodesToProcessList.add (node);
			}
		}
		
		//StringBuilder sb = new StringBuilder ();
		int maxMaxLevel = 0;
		
		for (int n = 0; n < nodesToProcessList.size(); n++) {
			final Object node = nodesToProcessList.get (n);
			final Set<Edge> edgeSet = model.getEdges (node);
			boolean dealtWith = true;
			int maxLevel = -1;
			for (Edge edge : edgeSet) {
				if (edge.getNode1() == node) {
					if (!nodesToProcess.contains (edge.getNode2()) && !ranks.containsKey (edge.getNode2())) {
						nodesToProcess.add (edge.getNode2 ());
						nodesToProcessList.add (edge.getNode2 ());
					}
				}
				else if (ranks.containsKey (edge.getNode1())) {
					maxLevel = Math.max (maxLevel, ranks.get (edge.getNode1()).intValue());
					maxMaxLevel = Math.max (maxLevel, maxMaxLevel);
				} else {
					dealtWith = false;
				}
			}
			
			
			if (dealtWith) {
				nodesToProcess.remove (node);
				ranks.put (node, Integer.valueOf (maxLevel + 1));
				/*
				sb.setLength (0);
				sb.append ("n: "+node+", v: "+(maxLevel + 1)+", parents: ");
				for (Edge edge : edgeSet) {
					if (edge.getNode2() == node) {
						sb.append ("["+edge.getNode1()+":"+ranks.get(edge.getNode1())+"],");
					}
				}
				LOGGER.debug (sb);
				*/
				
			}
			else {
				nodesToProcessList.add (node);
			}
		}
		
		maxMaxLevel++;
		
		final Set<Map.Entry<Object, Integer>> entries = ranks.entrySet();
		LOGGER.debug ("Entry size: "+entries.size());
		//for (Map.Entry<Object, Integer> entry : entries) {
		//	System.out.println ("n: "+entry.getKey()+", v: "+entry.getValue());
		//}
		
		LOGGER.info ("nodes to process set: "+nodesToProcess.size());
		//for (Object node : nodesToProcess) {
		//	LOGGER.debug ("n: "+node);
		//}
		
		LOGGER.info ("nodes to process list: "+nodesToProcessList.size());
		//for (Object node : nodesToProcessList) {
		//	System.out.println ("n: "+node);
		//}
		
		LOGGER.info ("maxMaxLevel: "+maxMaxLevel);
		
		for (int n = 0; n < maxMaxLevel + 1; n++) {
			layerLists.add (new ArrayList<Object> ());
		}
		
		for (Map.Entry<Object, Integer> entry : entries) {
			final int level = entry.getValue().intValue();
			layerLists.get(level).add (entry.getKey());
		}
		
		for (int n = 0; n < maxMaxLevel + 1; n++) {
			LOGGER.info ("LayerList "+n+" :"+layerLists.get(n));
		}
	}

	
	public void startingLayout (final GraphModel model, final ObjectPlacementMapping objMapping, final List<List<Object>> layerLists) {
		
		final Comparator<Object> edgeSorter = new EdgeDegreeComparator (model);
		final int totalNodes = model.getNodeCount();
		
		maxLayerWidth = 0;
		for (int layer = 0; layer < layerLists.size(); layer++) {
			maxLayerWidth = Math.max (maxLayerWidth, minObjSep * layerLists.get(layer).size());
		}
		
		
		for (int layer = 0; layer < layerLists.size(); layer++) {
			final int coord2 = 100 + (layer * layerSep);
						
			final List<Object> layerList = layerLists.get (layer);
			Collections.sort (layerList, edgeSorter);
			ArrayListUtil.riffle (layerList);
			
			final int midPoint = (int)(((double)layerList.size() / 2.0 / (double)totalNodes) * maxLayerWidth);
			final double multiplier = (double)totalNodes / (double)layerList.size();
			
			for (int nodeIndex = 0; nodeIndex < layerList.size(); nodeIndex++) {
				final Object node = layerList.get (nodeIndex);
				final double coord1 = (-midPoint + (((double)nodeIndex / (double)totalNodes) * maxLayerWidth)) * multiplier;
				//System.out.print ("["+layer+". edgeCount: "+model.getEdges(node).size()+", x: "+x+"]");
				final ObjectPlacement objPlacement = objMapping.getPlacement (node);
				final Point2D.Double location = objPlacement.getLocation ();
				location.x = (orientation ? coord1 : coord2);
				location.y = (orientation ? coord2 : coord1);
			}
			//System.out.println ();
		}
	}
	
	
	/**
	 * Small routine to compare current number of edge crossings with previous numbers
	 * to decide whether layout algorithm should terminate. Conditions include
	 * zero crossings, no change since last calculation, and strobing 
	 * (flipping repeatedly between two edge crossing totals)
	 * @param totalCrossings - historical record of crossing edge numbers
	 * @param newTotalCross - last instance of number of crossing edges
	 * @return boolean true if layout algorithm should terminate
	 */
	
	final protected boolean terminate (final int[] totalCrossings, final int newTotalCross) {
		//System.err.print (Arrays.toString(totalCrossings));
		//for (int n = 3; n > 0; n--) {
		//	totalCrossings [n] = totalCrossings [n - 1];
		//}
		System.arraycopy (totalCrossings, 0, totalCrossings, 1, 3);
		totalCrossings [0] = newTotalCross;
		//System.err.println (" " + Arrays.toString(totalCrossings));
		
		boolean terminate = (totalCrossings [0] == 0) || (totalCrossings [1] == totalCrossings [0]);
		terminate |= (totalCrossings[0] == totalCrossings[2]) && (totalCrossings [1] == totalCrossings [3]);	
		LOGGER.debug ("terminate: "+terminate+" tc: "+newTotalCross);
		return false;
		//return labelBorder;		
	}
	
	
	protected List<Set<Edge>> makeEdgeSets (final GraphModel model, final List<List<Object>> layerLists) {
		
		final List<Set<Edge>> edgeSets = new ArrayList<Set<Edge>> (layerLists.size());
		
		for (int layerListIndex = 0; layerListIndex < layerLists.size(); layerListIndex++) {
			final Set<Edge> edgeSet = new HashSet<Edge> ();
			edgeSets.add (edgeSet);
			final List<Object> layerList = layerLists.get (layerListIndex);
					
			for (int nodeIndex = 0; nodeIndex < layerList.size(); nodeIndex++) {
				final Object node = layerList.get (nodeIndex);
				final Set<Edge> edges = model.getEdges (node);
				edgeSet.addAll (edges);
			}
		}
		
		//LOGGER.debug ("EdgeSets: "+edgeSets);
		return edgeSets;
	}
	
	
	protected List<List<Edge>> makeLowestEdgeLists (final GraphModel model, final List<List<Object>> layerLists, 
			final Map<Object, Integer> ranks) {
		
		final List<List<Edge>> lowestEdgeLists = new ArrayList<List<Edge>> (layerLists.size());
		
		for (int layerListIndex = 0; layerListIndex < layerLists.size(); layerListIndex++) {
			final Set<Edge> edgeSet = new HashSet<Edge> ();	
			final List<Object> layerList = layerLists.get (layerListIndex);
					
			for (int nodeIndex = 0; nodeIndex < layerList.size(); nodeIndex++) {
				final Object node = layerList.get (nodeIndex);
				final Set<Edge> edges = model.getEdges (node);
				for (Edge edge : edges) {
					final int level1 = ranks.get(edge.getNode1()).intValue();
					final int level2 = ranks.get(edge.getNode1()).intValue();
					final int min = Math.min (level1, level2);
					if (ranks.get(edge.getNode1()).intValue() == min) {
						edgeSet.add (edge);
					}
				}
			}
			
			lowestEdgeLists.add (new ArrayList<Edge> (edgeSet));
		}
		//LOGGER.debug ("LowestEdgeLists: "+lowestEdgeLists);
		
		return lowestEdgeLists;
	}
	
	
	protected final void sort (final GraphModel model, final ObjectPlacementMapping objMapping, 
			final List<List<Object>> layerOrders, final Map<Object, Integer> ranks) {
		
		int edgeCalcs = 0, totalCross = 1;
		final int[] totalCrossings = new int [4];
		final int ITERATIONS = 300;	
			
		for (int cycle = 1; ((!terminate (totalCrossings, totalCross) 
				//|| ((cycle & 1) == 1)
				) && cycle < ITERATIONS); cycle++) {
			//LOGGER.debug ("cycle: "+cycle);
			final boolean downDirection = (cycle & 1) == 1;
			final List< List<AverageObject>> averageVals = new ArrayList< List<AverageObject>> (layerOrders.size());		
			for (int n = 0; n < layerOrders.size(); n++) {
				averageVals.add (new ArrayList<AverageObject> ());
			}
			
			//System.out.println ("gl: "+gridList.size()+", "+medianVals.size());
			final int lastN = downDirection ? 0: layerOrders.size() - 1;
			final int step = downDirection ? 1 : -1;
			final int start = lastN; // + step;
			final int finish = downDirection ? layerOrders.size() : 0;
			//int finish = direction ? gridList.size() : gridList.size() - 3;

			
			for (int level = start; level != finish; level += step) {
				//final RankObject thisRank = gridList.get (n);
				//final LayerOrder thisLayer = layerOrders.get (thisRank);	
				//final List<Object> thisLayer = layerOrders.get (level);	
				//List<MultiEdge> majorRankEdges = direction ? edgesByHighestRank.get (thisRank) : edgesByLowestRank.get (thisRank);
				//List<MultiEdge> minorRankEdges = direction ? edgesByLowestRank.get (thisRank) : edgesByHighestRank.get (thisRank);
				final Set<Edge> rankEdges = edgeSets.get (level);
				//if (cycle == 1) {
				//	LOGGER.debug ("rankedges: "+rankEdges);
				//}
				//LOGGER.debug ("thislayer: "+thisRank+", "+thisLayer.getListSize()+", re: "+rankEdges.size());
				
				if (rankEdges != null && !rankEdges.isEmpty()) {
					
					final Map <ObjectPlacement, List<Float>> primaryAxialPositions = new HashMap <ObjectPlacement, List<Float>> ();
					final Map <ObjectPlacement, List<Float>> secondaryAxialPositions = new HashMap <ObjectPlacement, List<Float>> ();
					
					for (final Edge we : rankEdges) {
						final Object drg = (ranks.get(we.getNode1()).intValue() == level) ? we.getNode1() : we.getNode2();
						final Object drgOther = (drg == we.getNode1()) ? we.getNode2() : we.getNode1();		
						final ObjectPlacement drgOp = objMapping.getPlacement (drg);
						final ObjectPlacement drgOtherOp = objMapping.getPlacement (drgOther);
						
						//System.out.println ("drg: "+drg+", drg ranK: "+ranks.get(drg)+", level: "+level);
						//System.out.println ("drgOther: "+drgOther+", drgOther ranK: "+ranks.get(drgOther)+", level: "+level);
						/*
						if (cycle == 1) {
							if (!drgOp.isDummy()) {
								LOGGER.debug ("drp: "+((ObjectPlacement)drgOp));
							}
							if (!drgOtherOp.isDummy()) {
								LOGGER.debug ("odrp: "+((ObjectPlacement)drgOtherOp));
							}
						}
						*/
						
						//final int rankIndex = ranks.get(drg).intValue();
						final int rankIndexOther = ranks.get(drgOther).intValue();
						//LOGGER.debug (drg+", ");
							
						if (!primaryAxialPositions.containsKey (drgOp)) {
							primaryAxialPositions.put (drgOp, new ArrayList<Float> ());
						}
						if (!secondaryAxialPositions.containsKey (drgOp)) {			
							secondaryAxialPositions.put (drgOp, new ArrayList<Float> ());
						}
						
						//final List<Object> gg = layerOrders.get (rankIndexOther);
						//final int index = (int)gg.getPoint(drgOther).getX();
						final Point2D.Double pOther = drgOtherOp.getLocation();
						final float axisCoord = orientation ? (float)pOther.x : (float)pOther.y;
						//LOGGER.debug ("x: "+index+", width: "+gg.getNodeSize().getWidth());
						//final int index = gg.getIndex(drgOther);
						//final float x = (float)(index + (gg.getNodeSize().getWidth() / 2.0));
							
						final boolean majorDir = downDirection ^ (rankIndexOther > level);
						
						final Map<ObjectPlacement, List<Float>> AxisPositions = majorDir ? primaryAxialPositions : secondaryAxialPositions;
						//BitSet weight = we.getWeight();
						//for (int m = weight.cardinality(); --m >= 0;) {
							AxisPositions.get(drgOp).add (axisCoord);
							
						/*
						if (rankIndex == rankIndexOther) {
							if (!majorXPositions.containsKey (drgOther)) {
								majorXPositions.put (drgOtherOp, new ArrayList<Float> ());
							}
							if (!minorXPositions.containsKey (drgOther)) {			
								minorXPositions.put (drgOtherOp, new ArrayList<Float> ());
							}
							
							//final List<Object> gg2 = layerOrders.get (drg.getRank());
							//final int index2 = (int)gg2.getPoint(drg).getX();
							final int index2 = (int)drgOp.getLocation().x;
							//final int index = gg.getIndex(drgOther);
							//final float x2 = (float)(index2 + (gg2.getNodeSize().getWidth() / 2.0));
							final float x2 = (float)(index2);
											
							final Map<ObjectPlacement, List<Float>> XPositions2 = majorDir ? majorXPositions : minorXPositions;
							//BitSet weight = we.getWeight();
							//for (int m = weight.cardinality(); --m >= 0;) {
								XPositions2.get(drgOther).add (x2);
						}
						*/
						//}
						//System.out.println ("major: "+majorDir+", "+drg+", "+nodeXVals);
						edgeCalcs++;
					}
							
					//LOGGER.debug ("\nmaj: "+majorXPositions.size()+", min: "+minorXPositions.size());
					
					final Set<Map.Entry<ObjectPlacement, List<Float>>> majorRepSet = primaryAxialPositions.entrySet();
					for (final Map.Entry<ObjectPlacement, List<Float>> entry : majorRepSet) {
						final ObjectPlacement drgOp = entry.getKey();
						final List<Float> major = entry.getValue();
						final List<Float> minor = secondaryAxialPositions.get (drgOp);
						final float majorAverage = MathUtils.getInstance().getMean (major);
						final float minorAverage = (cycle > 2 ? MathUtils.getInstance().getMean (minor) : 0.0f);						
						final AverageObject avgObj = new AverageObject (drgOp, majorAverage, minorAverage, major.size(), minor.size());
						averageVals.get(level).add (avgObj);	
						//LOGGER.debug (majorXPositions.get(drg).toString()+minorXPositions.get(drg).toString()+", "+mo.toString());
					}
				}
				
				final List<AverageObject> averages = averageVals.get (level);
				//LOGGER.debug (averages.toString());
				Collections.sort (averages);
				//LOGGER.debug (averages.toString());
				//LOGGER.debug (medians.toString());
				//LOGGER.debug ("gg: "+thisLayer.getSize()+", medList: "+medians.size());
				
				//thisLayer.resize();
				final int totalNodes = model.getNodeCount();
				final int midPoint = (int)(((double)averages.size() / 2.0 / (double)totalNodes) * maxLayerWidth);
				final double multiplier = (double)totalNodes / (double)averages.size();
				LOGGER.debug ("midpoint: "+midPoint+", averages,size: "+averages.size()+", totalNodes: "+totalNodes);
				for (int count = 0; count < averages.size(); count++) {
					final AverageObject avgObj = averages.get (count);
					final double axisCoord = (-midPoint + (((double)count / (double)totalNodes) * maxLayerWidth)) * multiplier;
					final Point2D.Double point = avgObj.getObjectPlacement().getLocation();
					if (orientation) {
						point.x = axisCoord;
					} else {
						point.y = axisCoord;
					}
					//System.out.println ("x: "+x+", mo: "+mo.getObjectPlacement().getLocation());
					//thisLayer.setRepIndex (count, mo.getObjectPlacement());
				}
				
			}		
			//Dimension total = countEdgeCrossings (allEdges, layerOrders);
			//LOGGER.debug ("edge crossings: "+total.toString());
			
			final Dimension total = countEdgeCrossings2 (objMapping, model, lowestEdgeLists, layerOrders, ranks, orientedAxisComp);
			LOGGER.debug ("edge crossings: "+total.toString());

			totalCross = total.height;
			LOGGER.debug ("cycle: "+cycle+", step: "+step+", tc: "+totalCross+", cl: "+Arrays.toString (totalCrossings));
		}		
		//System.out.println (edgeCalcs+" edge calcs");
	}

	
	/*
	protected Dimension countEdgeCrossings (
			final Set<Edge> allEdges,
			final List<List<Object>> layerOrders) {

		long ti = System.nanoTime();
		
		final Line2D line1 = new Line2D.Double ();
		final Line2D line2 = new Line2D.Double ();
		
		final List<MultiEdge> edges = new ArrayList<MultiEdge> (allEdges.keySet());
		int crossCount = 0;
		for (int n = 0; n < edges.size() - 1; n++) {
			final MultiEdge we1 = edges.get(n);
			
			for (int m = n + 1; m < edges.size(); m++) {
				final MultiEdge we2 = edges.get(m);
				
				final ObjectPlacement dr1a = we1.getFrom();
				final ObjectPlacement dr1b = we1.getTo();
				final ObjectPlacement dr2a = we2.getFrom();
				final ObjectPlacement dr2b = we2.getTo();
				
				if (!(dr1a == dr2a || dr1a == dr2b || dr1b == dr2a || dr1b == dr2b)) {
				
					final List<Object> gg1a = layerOrders.get(dr1a.getRank());
					final List<Object> gg1b = layerOrders.get(dr1b.getRank());
					final List<Object> gg2a = layerOrders.get(dr2a.getRank());
					final List<Object> gg2b = layerOrders.get(dr2b.getRank());
					
					final Point2D p1a = gg1a.getPoint (gg1a.getIndex(dr1a));
					final Point2D p1b = gg1b.getPoint (gg1b.getIndex(dr1b));
					final Point2D p2a = gg2a.getPoint (gg2a.getIndex(dr2a));
					final Point2D p2b = gg2b.getPoint (gg2b.getIndex(dr2b));
					
					final double startx = p1a.getX() + (gg1a.getNodeSize().getWidth() / 2.0);
					final double starty = (p1a.getY() > p1b.getY() ? p1a.getY() : p1a.getY() + gg1a.getNodeSize().height);
					final double endx = p1b.getX() + (gg1b.getNodeSize().getWidth() / 2.0);
					final double endy = (p1a.getY() > p1b.getY() ? p1b.getY() + gg1b.getNodeSize().height : p1b.getY());
					line1.setLine (startx, starty, endx, endy);
					
					final double startx2 = p2a.getX() + (gg2a.getNodeSize().getWidth() / 2.0);
					final double starty2 = (p2a.getY() > p2b.getY() ? p2a.getY() : p2a.getY() + gg2a.getNodeSize().height);
					final double endx2 = p2b.getX() + (gg2b.getNodeSize().getWidth() / 2.0);
					final double endy2 = (p2a.getY() > p2b.getY() ? p2b.getY() + gg2b.getNodeSize().height : p2b.getY());
					//LOGGER.debug (dr1a.getLocation()+", "+dr2a.getLocation());
					line2.setLine (startx2, starty2, endx2, endy2);
					
	
					if (line1.intersectsLine (line2)) {
						crossCount++;
						//LOGGER.debug ("["+n+","+m+"]\t"+we1.getFrom().getNameString()+":"+line1.getP1()+", "+line1.getP2()+"\t"+we2.getFrom().getNameString()+":"+line2.getP1()+", "+line2.getP2());
					}
				}
			}
		}
		
		ti = System.nanoTime() - ti;
		LOGGER.debug ("Edge crossings: "+ti/1E6+" ms.");
		
		return new Dimension (edges.size(), crossCount);
	}
	*/
	
	protected Dimension countEdgeCrossings2 (
			final ObjectPlacementMapping coordMap,
			final GraphModel model,
			final List<List<Edge>> edgesByLowestRank,
			final List<List<Object>> layerLists,
			final Map<Object, Integer> ranks,
			final Comparator<ObjectPlacement> objPosComparator) {
		
		long nanoTime = System.nanoTime();	
		int crossCount = 0;
		
		//Logger.debug ("rankList: "+rankList);
		final Comparator<Edge> edgeAxialComparator = new EdgeAxialComparator (coordMap, objPosComparator);
		
		for (int layerIndex = 0; layerIndex < edgesByLowestRank.size(); layerIndex++) {
			final List<Edge> edgeList = edgesByLowestRank.get (layerIndex);
			
			if (edgeList != null && !edgeList.isEmpty()) {
				Collections.sort (edgeList, edgeAxialComparator);
				
				//Logger.debug ("edgeList: "+edgeList);
	
				final List<ObjectPlacement> southNodes = new ArrayList<ObjectPlacement> ();
				final Map <ObjectPlacement, Integer> southOrder = new HashMap <ObjectPlacement, Integer> ();
				for (final Edge we : edgeList) {
					final int rank1 = ranks.get(we.getNode1()).intValue();
					final int rank2 = ranks.get(we.getNode2()).intValue();
					final ObjectPlacement southNode = (rank1 > rank2 
							? coordMap.getPlacement (we.getNode1())
							: coordMap.getPlacement (we.getNode2())
					);									
					southNodes.add (southNode);
					southOrder.put (southNode, null);
				}			
				//Logger.debug ("southNodes: "+southNodes);
				//Logger.debug ("southGroup: "+southOrder);
				
			
				if (southNodes.size() > 1) {
					
					final List<ObjectPlacement> southList = new ArrayList<ObjectPlacement> (southOrder.keySet());
					Collections.sort (southList, objPosComparator);					
					int axisOrder = 0;
					for (final ObjectPlacement drgOp : southList) {
						southOrder.put (drgOp, Integer.valueOf (axisOrder));
						axisOrder++;
					}
					
					//Logger.debug ("southList: "+southList);
					//Logger.debug ("southOrder: "+southOrder);
										
					int firstIndex = Integer.highestOneBit (southNodes.size() - 1) * 2;
					final int[] bilayerTree = new int [(firstIndex * 2) - 1];
					firstIndex--;
					/*
					for (final MultiEdge we : edgeList) {
						final DisplayRepGraph southNode = DummyRankComparator.getInstance().compare (we.getFrom().getRank(), we.getTo().getRank()) > 0
													? we.getFrom() : we.getTo();
						int southNodeIndex = firstIndex + southOrder.get(southNode).intValue();
						bilayerTree [southNodeIndex] ++;
						while (southNodeIndex > 0) {
							if (southNodeIndex % 2 == 1) {
								crossCount += bilayerTree [southNodeIndex + 1];
							}
							southNodeIndex = (southNodeIndex - 1) / 2;
							bilayerTree [southNodeIndex] ++;
						}
						
						//Logger.debug (Arrays.toString(bilayerTree));
					}
					*/
					for (int n = 0; n < edgeList.size(); n++) {
						final ObjectPlacement southNode = southNodes.get (n);
						int southNodeIndex = firstIndex + southOrder.get(southNode).intValue();
						bilayerTree [southNodeIndex] ++;
						while (southNodeIndex > 0) {
							if ((southNodeIndex & 1) == 1) {
								crossCount += bilayerTree [southNodeIndex + 1];
							}
							southNodeIndex = (southNodeIndex - 1) / 2;
							bilayerTree [southNodeIndex] ++;
						}
						
						//Logger.debug (Arrays.toString(bilayerTree));
					}
				}
			}
		}
	
		nanoTime = System.nanoTime() - nanoTime;
		LOGGER.debug ("Barth Edge crossings: "+nanoTime/1E6+" ms.");
	
		return new Dimension (model.getEdgeCount(), crossCount);
	}
	
	
	class AverageObject extends Object implements Comparable<AverageObject> {

		private final ObjectPlacement drg;
		private float majorMedian, minorMedian;
		private int majorDegree, minorDegree;
		
		AverageObject (final ObjectPlacement rep) {
			super ();
			drg = rep;
			majorMedian = 0.0f;
			minorMedian = 0.0f;
		}
		
		AverageObject (final ObjectPlacement rep, final float majorMedian, final float minorMedian, final int majorDegree, final int minorDegree) {
			this (rep);
			this.majorMedian = majorMedian;
			this.minorMedian = minorMedian;
			this.majorDegree = majorDegree;
			this.minorDegree = minorDegree;
		}


		protected ObjectPlacement getObjectPlacement () {
			return drg;
		}

		protected float getMajorMedian () {
			return majorMedian;
		}

		protected void setMajorMedian (final float majorMedian) {
			this.majorMedian = majorMedian;
		}

		protected float getMinorMedian () {
			return minorMedian;
		}

		public void setMinorMedian (final float minorMedian) {
			this.minorMedian = minorMedian;
		}
		
		protected final int getMajorDegree() {
			return majorDegree;
		}

		protected final int getMinorDegree() {
			return minorDegree;
		}

		protected final void setMajorDegree (final int majorDegree) {
			this.majorDegree = majorDegree;
		}

		protected final void setMinorDegree (final int minorDegree) {
			this.minorDegree = minorDegree;
		}
		
		@Override
		public int compareTo (final AverageObject avgObj) {
			int comp = (majorMedian > avgObj.getMajorMedian()) ? 1 : (majorMedian < avgObj.getMajorMedian() ? -1 :0);
			if (comp == 0) {
				comp = (minorMedian > avgObj.getMinorMedian()) ? 1 : (minorMedian < avgObj.getMinorMedian() ? -1 :0);
				if (comp == 0) {
					comp = majorDegree - avgObj.getMajorDegree();
					if (comp == 0/* && !drg.isDummy() && !mo.getObjectPlacement().isDummy()*/) {
						//v = drg.getFNI().getLayerMembership() - mo.getDrg().getFNI().getLayerMembership();
						//if (v == 0) {
						 	//v = 
							//v = DisplayRepGraphComparators.nameComparator.compare(drg, mo.getObjectPlacement());
						//}
					}
				}
			}
			return comp;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + majorDegree;
			result = prime * result + Float.floatToIntBits(majorMedian);
			result = prime * result + Float.floatToIntBits(minorMedian);
			return result;
		}

		@Override
		public boolean equals (final Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			
			final AverageObject other = (AverageObject) obj;
			if (!getOuterType().equals(other.getOuterType())) {
				return false;
			}
			if (majorDegree != other.majorDegree
					|| Float.floatToIntBits(majorMedian) != Float.floatToIntBits(other.majorMedian) 
					|| Float.floatToIntBits(minorMedian) != Float.floatToIntBits(other.minorMedian)) {
				return false;
			}
			return true;
		}
		
		@Override
		public String toString() {
			return ("maj: "+majorMedian+", min: "+minorMedian+", drg: "+drg.toString());
		}

		private MedianSortHoriz getOuterType() {
			return MedianSortHoriz.this;
		}
	}

	

	@Override
	public Object getNearestTo (final JGraph graph, final Point point) {
		return null;
	}
	
	
	static class OPXComparator implements Comparator<ObjectPlacement> {

		@Override
		public int compare (final ObjectPlacement op1, final ObjectPlacement op2) {
			final double diff = op1.getLocation().x - op2.getLocation().x;
			return (diff > 0.0 ? 1 : (diff < 0.0 ? -1 : 0));
		}
		
	}
	
	static class OPYComparator implements Comparator<ObjectPlacement> {

		@Override
		public int compare (final ObjectPlacement op1, final ObjectPlacement op2) {
			final double diff = op1.getLocation().y - op2.getLocation().y;
			return (diff > 0.0 ? 1 : (diff < 0.0 ? -1 : 0));
		}
		
	}
	
	class EdgeAxialComparator implements Comparator<Edge> {

		protected ObjectPlacementMapping objMapping;
		protected Comparator<ObjectPlacement> axisComparator;
		
		public EdgeAxialComparator (final ObjectPlacementMapping objMapping, final Comparator<ObjectPlacement> axisComparator) {
			this.objMapping = objMapping;
			this.axisComparator = axisComparator;
		}
		
		@Override
		public int compare (final Edge edge1, final Edge edge2) {
			final ObjectPlacement dr1From = objMapping.getPlacement (edge1.getNode1 ());
			final ObjectPlacement dr1To = objMapping.getPlacement (edge1.getNode2 ());
			final ObjectPlacement dr2From = objMapping.getPlacement (edge2.getNode1 ());
			final ObjectPlacement dr2To = objMapping.getPlacement (edge1.getNode2 ());
			
			final int ranke1n1 = ranks.get (edge1.getNode1()).intValue();
			final int ranke1n2 = ranks.get (edge1.getNode2()).intValue();
			final ObjectPlacement dr1North = (ranke1n1 < ranke1n2 ? dr1From : dr1To);
			final ObjectPlacement dr1South = (dr1From == dr1North) ? dr1To : dr1From;
			
			final int ranke2n1 = ranks.get (edge2.getNode1()).intValue();
			final int ranke2n2 = ranks.get (edge2.getNode2()).intValue();
			final ObjectPlacement dr2North = (ranke2n1 < ranke2n2 ? dr2From : dr2To);
			final ObjectPlacement dr2South = (dr2From == dr2North) ? dr2To : dr2From;

			int val = axisComparator.compare (dr1North, dr2North);
			if (val == 0) {
				val = axisComparator.compare (dr1South, dr2South);
			}
			return val;
		}
	}
	
	
	
	static class EdgeDegreeComparator implements Comparator<Object> {

		protected GraphModel graphModel;
		
		public EdgeDegreeComparator (final GraphModel model) {
			graphModel = model;
		}
		
		@Override
		public int compare (final Object obj1, final Object obj2) {
			final int size1 = graphModel.getEdges(obj1).size();
			final int size2 = graphModel.getEdges(obj2).size();
			return size1 - size2;
		}
	}
}