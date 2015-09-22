package example.matrix;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
* Parses nexus text file through various methods into arguments that can be passed to {@link BaseForest Forest} methods.
* @author Martin Graham
* @version application
*/


public final class EvaluationDOM {

	public static Element getEvaluationElement (final Document doc) {
		return doc.getDocumentElement();
	}

	public static String getRootSkillName (final Element elem) {

		 Node child = elem.getFirstChild();

		 while (child.getNodeType() != Node.ELEMENT_NODE) {    // Skip comment nodes
		 	child = child.getNextSibling();
		 }

	     final Element rootSkill = (Element)child;
	     return rootSkill.getAttribute ("skillName");
	}

	public static Element getSkillByName (final Element elem, final String skillName) {

	  	final NodeList nodeList = getSkillNodeList (elem);
	  	Element returnNode = null;

		for (int k = 0; k < nodeList.getLength(); k++) {
        	final Node node = nodeList.item (k);
        	if (((Element)node).getAttribute("skillName").equalsIgnoreCase (skillName)) {
        		returnNode = (Element)node;
        		k = nodeList.getLength();
        	}
        }

        return returnNode;
    }

	public static NodeList getSkillNodeList (final Element elem) {
		return elem.getElementsByTagName ("idSkill");
	}

    public static String getPCDataFromElement (final Element elem) {
    	final Node node = elem.getFirstChild ();
		return node == null ? null : ((CharacterData) node).getData ();
	}
}