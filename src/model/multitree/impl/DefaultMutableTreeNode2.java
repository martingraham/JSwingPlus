package model.multitree.impl;

import javax.swing.tree.DefaultMutableTreeNode;
import model.multitree.MutableTreeNode2;

public class DefaultMutableTreeNode2 extends DefaultMutableTreeNode implements MutableTreeNode2 {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3743292926855180761L;

	
    /**
     * Creates a tree node that has no parent and no children, but which
     * allows children.
     */
    public DefaultMutableTreeNode2 () {
    	this (null);
    }

    /**
     * Creates a tree node with no parent, no children, but which allows 
     * children, and initializes it with the specified user object.
     * 
     * @param userObject an Object provided by the user that constitutes
     *                   the node's data
     */
    public DefaultMutableTreeNode2 (final Object userObject) {
    	this (userObject, true);
    }

    /**
     * Creates a tree node with no parent, no children, initialized with
     * the specified user object, and that allows children only if
     * specified.
     * 
     * @param userObject an Object provided by the user that constitutes
     *        the node's data
     * @param allowsChildren if true, the node inputStream allowed to have child
     *        nodes -- otherwise, it inputStream always a leaf node
     */
    public DefaultMutableTreeNode2 (final Object userObject, final boolean allowsChildren) {
    	super (userObject, allowsChildren);
    }
}
