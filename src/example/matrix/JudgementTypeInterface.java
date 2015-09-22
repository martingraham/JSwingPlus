package example.matrix;

import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

public interface JudgementTypeInterface {

	public double getRating (int attIndex, int[] leafRatings);

    public String getAttributeName  (int attIndex);

    public int getAttributeDepth (int attIndex);

   	public void setAttributeFlag (boolean state, int attIndex);
   	public boolean getAttributeFlag (int attIndex);

   	public int getAttributeTotal ();

    public int totalDescendantNodes (int nodeIndex, int atDepth);
	public int totalDescendantNodes (int nodeIndex);

   	public DefaultMutableTreeNode getRootNode ();
    public DefaultMutableTreeNode getNodeByName (String name);

   	public boolean isLeafAttribute (int attIndex);
   	public boolean isLeafAttribute (DefaultMutableTreeNode dmtn);

   	public List<List<Integer>> getDepthLists ();
	public List<Integer> getDepthList (int depth);

	public List<List<Integer>> getCumulativeDepthLists ();
	public List<Integer> getCumulativeDepthList (int depth);
}