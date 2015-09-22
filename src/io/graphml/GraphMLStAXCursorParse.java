package io.graphml;

import io.AbstractStAXCursorParse;
import io.DataPrep;
import io.graphml.GraphMLTagConstants.MyTags;


import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.DefaultTreeModel;

import org.apache.log4j.Logger;

import model.multitree.impl.DefaultMutableTreeNode2;

//import example.multitree.TaxonUserObject;



/**
 * CursorParse sample inputStream used to demonstrate the use
 * of STAX cursor approach. In this approach application
 * instructs the parser to read the next event in the XML
 * input stream by calling <code>next()</code>.
 *
 * Note that <code>next()</code> just returns an integer constant
 * corresponding to underlying event where the parser inputStream positioned.
 * Application needs to call relevant function to get more
 * information related to the underlying event.
 *
 * You can imagine this approach as a virtual cursor moving across
 * the XML input stream. There are various accessor methods which
 * can be called when that virtual cursor inputStream at particular event.
 *
 *
 * @author <a href="neeraj.bajaj@sun.com">Neeraj Bajaj</a> Sun Microsystems,inc.
 *
 */

public class GraphMLStAXCursorParse extends AbstractStAXCursorParse {

	final static Logger LOGGER = Logger.getLogger (GraphMLStAXCursorParse.class);
	
    private final EnumSet<MyTags> startTags = GraphMLTagConstants.startTags;
    private final EnumSet<MyTags> endTags = GraphMLTagConstants.endTags;
    private final Map<String, String> rankIDs = new HashMap<String, String> ();
    private final Map<String, String> treeIDs = new HashMap<String, String> ();
    private final Map<Long, Object> userObjects = new HashMap<Long, Object> ();
    private final Stack<MutableTreeNode> parentStack;
    private String tempRankName = null, tempRankValue = null, tempTreeID = null;
    private int currentTreeIndex = -1;
   // private TaxonUserObject rootUserObj = new TaxonUserObject ("Root", "Root");
	//private long specID = 0;



    public GraphMLStAXCursorParse (final Object populateMe) {
        super (populateMe);
        parentStack = new Stack<MutableTreeNode> ();
    }

    public static void main (final String[] args) {
        final List<TreeModel> treeList = new ArrayList<TreeModel> ();
        final GraphMLStAXCursorParse cp = new GraphMLStAXCursorParse (treeList);
        LOGGER.info ("Filename: "+args[0]);
        try {
            cp.parse (DataPrep.getInstance().getPossiblyZippedInputStream (args[0], true));
        } catch (final Exception e) {
        	LOGGER.error ("error in cursorparse parse: " + e.toString());
        }

        /*
        if (args.length > 1 && (args[1].equals ("TOTCS"))) {
        	try {
        		final Writer w = DataPrep.getInstance().makeBufferedPrintWriter (new File ("output.xml"), XMLConstants2.UTF8, false);
        		new XMLTCSOutput101().write (bfi, w);
        	} catch (final IOException ioe) {
        		LOGGER.debug ("IOException: "+ioe.toString());
        	} 	
       	}
       	*/
        System.exit (0);
    }



    @Override
	protected void specificProcessStartElement (final String tagName, final Map<String, String> attrStore){
        	
        	
        	final MyTags tag = GraphMLTagConstants.tagMatcher.get (tagName);
        	
            if (startTags.contains (tag)) {
            	
            	List<TreeModel> treeList = (List<TreeModel>) populateMe;

               switch (tag) {

                case NAMES:
                     ipb.setText ("Reading Names");
                     break;

                case TREES:
                     //specID = bfi.getNode(bfi.getTotalNodes() - 1).getID() + 1;
                     break;

                case TREE:
                     tempTreeID = attrStore.get ("TREE_ID");
                     currentTreeIndex++;
                     ipb.setText ("Reading Hierarchy "+(currentTreeIndex + 1));
                     break;

                case TREE_NODE:
                    final String childID = attrStore.get ("NAME_IDREF");
                    final String statusID = attrStore.get ("STATUS");
                    LOGGER.debug ("nid: "+childID+", stat: "+statusID);
                    final Long cid = Long.valueOf (childID);
                    TreeModel tModel = treeList.get (currentTreeIndex);
                    MutableTreeNode newNode;
                    
                    if (!parentStack.empty()) {
                       final MutableTreeNode parentNode = parentStack.peek();
                       newNode = new DefaultMutableTreeNode ();
                       Object userObject = userObjects.get (cid);
                       newNode.setUserObject (userObject);
                       //newNode.setParent (parentNode);
                       parentNode.insert (newNode, parentNode.getChildCount());
                       LOGGER.debug ("Tree: "+tModel+", parentNode: "+parentNode+" --> newNode: "+newNode);
                    } else {
                       newNode = new DefaultMutableTreeNode ();
                       Object userObject = userObjects.get (cid);
                       newNode.setUserObject (userObject);
                       ((DefaultTreeModel)tModel).setRoot (newNode);
                    }

                    parentStack.add (newNode);
                    break;

                case CONCEPTS:
                     ipb.setText ("Reading Concepts");
                     break;

                default:
                     break;
                }
        }
    }




    @Override
	protected void specificProcessEndElement (final String tagName, final Map<String, String> attrStore) {
    		
        	
        	final MyTags tag = GraphMLTagConstants.tagMatcher.get (tagName);
        	
            if (endTags.contains (tag)) {

            	List<TreeModel> treeList = (List<TreeModel>) populateMe;
            	
               switch (tag) {

               case NAME:
                  final String nameID = attrStore.get ("NAME_ID");
                  //final long nID = Long.parseLong (nameID);
                  LOGGER.debug ("nid: ["+nameID+"]");
                 // TaxonUserObject tuo = new TaxonUserObject (text, rankIDs.get (attrStore.get ("RANK_IDREF")));
                  //userObjects.put (nID, tuo);
                  break;

               case TREE_NAME:
            	  MutableTreeNode root = new DefaultMutableTreeNode2 (/*rootUserObj*/);
            	  parentStack.add (root);
            	  TreeModel tModel = new DefaultTreeModel (root);
            	  treeList.add (tModel);
                  //bfi.addHierarchy (text, null, String2Regex.extractDateFromString (text));
                  treeIDs.put (tempTreeID, text);
                  break;

               case TREE_NODE:
                  parentStack.pop();
                  break;

               case RANK:
                  //bfi.getRanksObject().addRankName (Integer.parseInt (tempRankValue), tempRankName);
                  rankIDs.put (attrStore.get ("RANK_ID"), tempRankName);
                  break;

               case RANK_NAME:
                  tempRankName = text;
                  break;

               case RANK_VALUE:
                  tempRankValue = text;
                  break;

            	case CONCEPT:
            		/*
                  final String fromNameIDREF = attrStore.get ("FROM_NAME_IDREF");
                  final String fromTreeIDREF = attrStore.get ("FROM_TREE_IDREF");
                  final String toNameIDREF = attrStore.get ("TO_NAME_IDREF");
                  final String toTreeIDREF = attrStore.get ("TO_TREE_IDREF");

                  final long fID = Long.parseLong (fromNameIDREF);
                  final long tID = Long.parseLong (toNameIDREF);
                  final ForestNodeInterface ffni = bfi.getNodeByID (fID);
                  final ForestNodeInterface tfni = bfi.getNodeByID (tID);

                  final int ftree = bfi.getLayerModelIndex (treeIDs.get (fromTreeIDREF));
                  final int ttree = bfi.getLayerModelIndex (treeIDs.get (toTreeIDREF));
                  final ForestNodeLayer ffnl = ffni.getTreeLayerNo (ftree);
                  final ForestNodeLayer tfnl = tfni.getTreeLayerNo (ttree);

                  final int conceptRelationType = Integer.parseInt (text);
                  bfi.addNonHierarchicalLink (ffnl, tfnl, null, conceptRelationType);
                  //ffnl.setAcceptanceStatus ((byte) 1);
                  //tfnl.setAcceptanceStatus ((byte) 5);
                   */
                  break;

               case SPECIMEN_COUNT:
            	   /*
                  final Integer sCount = Integer.valueOf (text);
                  final long nodeID = parentStack.peek ();
                  final ForestNodeInterface fni = bfi.getNodeByID (nodeID);
                  final ForestNodeLayer fnl = fni.getTreeLayerNo (currentTreeIndex);
                  ((MetaForestNodeLayer)fnl).setComparableObject (CountData.getCountData (sCount));
                  //for (int n = sCount.intValue(); --n >= 0;) {
                  //    ForestNodeInterface specNode = bfi.addForestNode (fni.getName()+" spec. "+tempTreeID+Integer.toString (n + 1), specID, "Specimen");
                  //    bfi.addTreeLink (fni, specNode, currentTreeIndex, fnl.getAcceptanceStatus(), (byte)1);
                  //    specID++;
                  //}
                   */
                  break;

               default:
                  break;

               }
            }
    }

    public static String getDescription () {
    	return ("Multiple Tree Format Files");
    }
    
    public static String getSuffix (final boolean zipped) {
    	return (zipped ? ".xmt" : ".xmz");
    }
}