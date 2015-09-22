package example.matrix;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



public class AbstractJudgementType extends Object implements JudgementTypeInterface {

    protected transient DefaultMutableTreeNode[] treeNodeList;
    private transient final BitSet flags;
    private transient double potential;
    protected transient List<List<Integer>> depthLists;
    protected transient List<List<Integer>> cumulativeDepthLists;
    protected transient Map<String, DefaultMutableTreeNode> nameIndex;

    private static final Logger LOGGER = Logger.getLogger (AbstractJudgementType.class);


    public AbstractJudgementType (final Element elem) {
    	super ();
        final NodeList nodeList = EvaluationDOM.getSkillNodeList (elem);
        final int size = nodeList.getLength ();

        treeNodeList = new DefaultMutableTreeNode [size];
        flags = new BitSet (size);
        flags.set (0, size);

		nameIndex = new HashMap<String, DefaultMutableTreeNode> ();
		depthLists = new ArrayList<List<Integer>> ();
		cumulativeDepthLists = new ArrayList<List<Integer>> ();
		final List<ParentChildNodePair> list = new ArrayList<ParentChildNodePair> ();
		list.add (new ParentChildNodePair (null, nodeList.item(0), 1.0));
		createJudgementTree (list, 0, 0);
		buildCumulativeDepthLists (cumulativeDepthLists, treeNodeList[0], 0);
    }


	private void createJudgementTree (final Iterable<ParentChildNodePair> parentChildNodePairs, 
			final int initialIndex, final int depth) {

    	final List<ParentChildNodePair> nextLevel = new ArrayList<ParentChildNodePair> ();
    	int index = initialIndex;
    	
    	if (depthLists.size() <= depth) {
    		depthLists.add (new ArrayList<Integer> ());
    		cumulativeDepthLists.add (new ArrayList<Integer>());
     	}

    	for (ParentChildNodePair pcnp : parentChildNodePairs) {
        	final Node node = pcnp.getChild();
        	final DefaultMutableTreeNode parentTreeNode = pcnp.getParent();
        	final double weighting = pcnp.getWeighting ();

			final String name = ((Element)node).getAttribute ("skillName");
			final OntologyObject ontoObj = new OntologyObject (name, weighting, index, depth);
	    	treeNodeList [index] = new DefaultMutableTreeNode (ontoObj);
	    	nameIndex.put (name, treeNodeList [index]);
	    	final Integer intObj = Integer.valueOf (index);
	    	depthLists.get(depth).add (intObj);	// add this skill to a depth list

	    	LOGGER.debug ("depth: "+depth+", skill: "+ontoObj.name+", list size:"+(depthLists.get (depth)).size());

        	if (parentTreeNode != null) {
           		parentTreeNode.add (treeNodeList [index]);
        	}

        	final NodeList nodeList = node.getChildNodes();

        	// Count children that are idSkill Elements
        	int skillChildren = 0;
        	for (int k = 0; k < nodeList.getLength(); k++) {
        		final Node cnd = nodeList.item (k);
        		if (cnd.getNodeType() == Node.ELEMENT_NODE && 
        			((Element)cnd).getTagName().equalsIgnoreCase ("idSkill")) {
        			skillChildren++;
				}
        	}

			// Add children that are idSkill Elements to list (also using info calculated in last loop)
        	for (int k = 0; k < nodeList.getLength(); k++) {
        		final Node cnd = nodeList.item (k);
        		if (cnd.getNodeType() == Node.ELEMENT_NODE && 
        			((Element)cnd).getTagName().equalsIgnoreCase ("idSkill")) {
        			nextLevel.add (new ParentChildNodePair (treeNodeList [index], cnd,
        					weighting / skillChildren));
				}
        	}

        	index++;
        }

		if (!nextLevel.isEmpty()) { createJudgementTree (nextLevel, index, depth + 1); }
	}



	public final void buildCumulativeDepthLists (final List<List<Integer>> cumulativeDepthLists, final DefaultMutableTreeNode dmtn, final int depth) {

	 	for (int n = depth; n < cumulativeDepthLists.size(); n++) {
	 		final OntologyObject ontoObj = (OntologyObject)dmtn.getUserObject();
	 		(cumulativeDepthLists.get (n)).add (Integer.valueOf (ontoObj.listIndex));
	 		LOGGER.info ("depth: "+n+", skill: "+ontoObj.name+", list size:"+cumulativeDepthLists.get(n).size());
	 	}

        for (int n = 0; n < dmtn.getChildCount(); n++) {
            buildCumulativeDepthLists (cumulativeDepthLists, (DefaultMutableTreeNode) dmtn.getChildAt (n), depth + 1);
        }
	}


	public double getRating (final int attIndex, final int ratings[]) {

	       potential = 0.0;
           final double score = getRatingRecurse (treeNodeList [attIndex], attIndex, ratings);
           return (potential < 0.00000001) ? 0.0 : score / potential;
    }


    public double getRatingRecurse (final DefaultMutableTreeNode dmtn, final int attIndex, final int ratings[]) {

           final OntologyObject ontoObj = (OntologyObject)dmtn.getUserObject();
           double score = 0.0;

           if (!getAttributeFlag (attIndex)) {
                 score = 0.0;
            }
           else if (dmtn.isLeaf()) {
                potential += ontoObj.weight;
                score = ontoObj.weight * ratings [attIndex];
           }
           else {
                DefaultMutableTreeNode child;
                for (int n = dmtn.getChildCount(); --n >= 0;) {
                    child = (DefaultMutableTreeNode) dmtn.getChildAt (n);
                    score += getRatingRecurse (child, ((OntologyObject)child.getUserObject()).listIndex, ratings);
                }
            }

            return score;
    }

	private int totalDescendantNodes (final DefaultMutableTreeNode dmtn, final int atDepth) {

		int descendants = 1;
		for (int n = dmtn.getChildCount(); --n >= 0;) {
            final DefaultMutableTreeNode child = (DefaultMutableTreeNode) dmtn.getChildAt (n);
            final OntologyObject ontoObj = (OntologyObject) child.getUserObject();
            if (ontoObj.depth <= atDepth) {
            	descendants += totalDescendantNodes (child, atDepth);
            }
        }

        if (descendants > 1) { descendants--; }

        return descendants;
	}


    public int totalDescendantNodes (final int nodeIndex, final int atDepth) { return totalDescendantNodes (treeNodeList [nodeIndex], atDepth); }

	public int totalDescendantNodes (final int nodeIndex) { return totalDescendantNodes (nodeIndex, Integer.MAX_VALUE); }

    public boolean isLeafAttribute (final int attIndex) { return treeNodeList[attIndex].isLeaf(); }

    public boolean isLeafAttribute (final DefaultMutableTreeNode dmtn) { return dmtn.isLeaf(); }

    public int getAttributeTotal() { return treeNodeList.length; }

    public void setAttributeFlag (final boolean state, final int attIndex) {

        DefaultMutableTreeNode child = null;
		final DefaultMutableTreeNode dmtn = treeNodeList [attIndex];
           flags.set (attIndex, state);

           if (!dmtn.isLeaf()) {
                for (int n = dmtn.getChildCount(); --n >= 0;) {
                    child = (DefaultMutableTreeNode) dmtn.getChildAt (n);
                    setAttributeFlag (state, ((OntologyObject)child.getUserObject()).listIndex);
                }
            }
    }


   	public boolean getAttributeFlag (final int attIndex) { return flags.get (attIndex); }

    public String getAttributeName (final int attIndex) {
		return ((OntologyObject)treeNodeList[attIndex].getUserObject()).name;
	}

	public int getAttributeDepth (final int attIndex) {
		return ((OntologyObject)treeNodeList[attIndex].getUserObject()).depth;
	}

	public DefaultMutableTreeNode getNodeByName (final String name) {
		return (DefaultMutableTreeNode) nameIndex.get (name);
	}

    public DefaultMutableTreeNode getRootNode () { return treeNodeList [0]; }

	public List<List<Integer>> getDepthLists () { return depthLists; }

	public List<Integer> getDepthList (final int depth) { return depthLists.get (depth); }

 	public List<List<Integer>> getCumulativeDepthLists () { return cumulativeDepthLists; }

	public List<Integer> getCumulativeDepthList (final int depth) { return cumulativeDepthLists.get (depth); }

	protected static class ParentChildNodePair {

		private final Node child;
		private final DefaultMutableTreeNode parent;
		private final double weighting;

		public ParentChildNodePair (final DefaultMutableTreeNode parentNode, final Node childNode, 
				final double weight) {
			child = childNode;
			parent = parentNode;
			weighting = weight;
		}

		public DefaultMutableTreeNode getParent() { return parent; }
		public Node getChild() { return child; }
		public double getWeighting () { return weighting; }
	}
}