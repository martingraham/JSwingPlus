package example.matrix;

import javax.swing.tree.DefaultMutableTreeNode;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class IndividualJudgement extends Object implements Comparable<IndividualJudgement> {
	
    int[] ratings;
    private double overallRatingCached;
    JudgementTypeInterface jti;

    public IndividualJudgement (final Element elem, final JudgementTypeInterface judgementType) {
    	super ();
		this.jti = judgementType;
        ratings = new int [jti.getAttributeTotal()];
		final NodeList nodeList = EvaluationDOM.getSkillNodeList (elem);
	    fillJudgement (nodeList);
	    overallRatingCached = -2;
	}


    private final void fillJudgement (final NodeList skillNodes) {

  	  	for (int n = jti.getAttributeTotal(); --n >= 0;) {
    		setAttributeValue (-1, n);
    	}

    	for (int n = skillNodes.getLength(); --n >= 0;) {
    		final Node skillNode = skillNodes.item (n);
    		final String name = ((Element)skillNode).getAttribute("skillName");
    		final DefaultMutableTreeNode judgementTreeNode = jti.getNodeByName (name);

			if (judgementTreeNode != null) {
    			final OntologyObject ontoObject = (OntologyObject) judgementTreeNode.getUserObject();

    			final NodeList nodeList = skillNode.getChildNodes();
    			int value = -1;

    			for (int m = nodeList.getLength(); --m >= 0;) {
    				final Node valNode = nodeList.item (m);

	 				if (valNode.getNodeType() == Node.ELEMENT_NODE) {
    	   				final Element elem = (Element) valNode;

    	   				if (elem.getTagName().equalsIgnoreCase("Valuation")) {
    	   					final String potVal = EvaluationDOM.getPCDataFromElement (elem);
    	   					if (potVal != null) {
    							value = Integer.parseInt (potVal);
    						}
    	   				}
    	   			}
    			}

				setAttributeValue (value, ontoObject.listIndex);
			}
    	}
	}



	public double getOverallRating () {
		if (overallRatingCached < -1.5) {
			overallRatingCached = jti.getRating (0, ratings);
		}
		return overallRatingCached;
	}

	public void activeAttributeSetHasChanged () { overallRatingCached = -2.0; }

    public int getAttributeTotal() { return jti.getAttributeTotal(); }

    public final void setAttributeValue (final int value, final int attIndex) { ratings [attIndex] = value; }
    //public double getAttributeValue  (int attIndex) { return jti.getAttributeFlag (attIndex) ? (double)ratings[attIndex] : 0; }
    public double getAttributeValue  (final int attIndex) { return jti.getRating (attIndex, ratings);  }

    public void setAttributeFlag (final boolean state, final int attIndex) { jti.setAttributeFlag (state, attIndex); }
   	public boolean getAttributeFlag (final int attIndex) { return jti.getAttributeFlag (attIndex); }

    public String getAttributeName (final int attIndex) { return jti.getAttributeName (attIndex); }

    public DefaultMutableTreeNode getRoot () { return jti.getRootNode(); }
    
    public JudgementTypeInterface getJudgementType () { return jti; }


	@Override
	public int compareTo (final IndividualJudgement iJudgement) {
		return this.getOverallRating() > iJudgement.getOverallRating() ? -1 
				: (this.getOverallRating() < iJudgement.getOverallRating() ? 1 : 0);
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		final long temp = Double.doubleToLongBits(overallRatingCached);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}


	@Override
	public boolean equals (final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final IndividualJudgement other = (IndividualJudgement) obj;
		if (Double.doubleToLongBits(overallRatingCached) != Double
				.doubleToLongBits(other.overallRatingCached)) {
			return false;
		}
		return true;
	} 
}