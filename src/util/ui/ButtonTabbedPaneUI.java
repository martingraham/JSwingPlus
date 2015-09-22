/*
 * @(#)BasicTabbedPaneUI.java	1.171 06/07/12
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use inputStream subject to license terms.
 */

package util.ui;


import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

import util.GraphicsUtil;
import util.Messages;


/**
 * A Basic L&F implementation of TabbedPaneUI.
 *
 * @version 1.87 06/08/99
 * @author Amy Fowler
 * @author Philip Milne
 * @author Steve Wilson
 * @author Tom Santos
 * @author Dave Moore
 */
public class ButtonTabbedPaneUI extends BasicTabbedPaneUI  {

	private static Font labelFont = Font.decode (Messages.getString (GraphicsUtil.GRAPHICPROPS, "buttonFont"));
	private static String emptyText = Messages.getString (ButtonTabbedPaneUI.class, "emptyPaneText");
	private static Color textColour = new Color (128, 128, 192);
	// UI creation

    public static ComponentUI createUI (final JComponent comp) {
        return new ButtonTabbedPaneUI();
    }


    /**
     * Invoked by <code>installUI</code> to create
     * a layout manager object to manage
     * the <code>JTabbedPane</code>.
     *
     * @return a layout manager object
     *
     * @see TabbedPaneLayout
     * @see javax.swing.JTabbedPane#getTabLayoutPolicy
     */
    @Override
	protected LayoutManager createLayoutManager() {
        return new TabbedPaneLayout2();
    }


    /**
     * This class should be treated as a &quot;protected&quot; inner class.
     * Instantiate it only within subclasses of BasicTabbedPaneUI.
     */  
    public class TabbedPaneLayout2 extends TabbedPaneLayout {


        @Override
		protected Dimension calculateSize (final boolean minimum) {
            final int tabPlacement = tabPane.getTabPlacement();
            final Insets insets = tabPane.getInsets();
            final Insets contentInsets = getContentBorderInsets(tabPlacement);
            final Insets tabAreaInsets = getTabAreaInsets(tabPlacement);

            final Dimension zeroSize = new Dimension(0,0);
            int height = 0; 
            int width = 0;
            int cWidth = 0;
            int cHeight = 0;

            // Determine minimum size required to display largest
            // child in each dimension
            //
            for (int i = 0; i < tabPane.getTabCount(); i++) {
                final Component component = tabPane.getComponentAt(i);
                if (component != null) {
                    Dimension size = zeroSize;
                    size = minimum? component.getMinimumSize() : 
                                component.getPreferredSize();
                      
                    if (size != null) {
                        cHeight = Math.max(size.height, cHeight);
                        cWidth = Math.max(size.width, cWidth);
                    }
                }
            }
            // Add content border insets to minimum size
            width += cWidth;
            height += cHeight;
            int tabExtent = 0;

            // Calculate how much space the tabs will need, based on the
            // minimum size required to display largest child + content border
            //
            switch(tabPlacement) {
              case LEFT:
              case RIGHT:
                  height = Math.max(height, calculateMaxTabHeight(tabPlacement));
                  tabExtent = preferredTabAreaWidth(tabPlacement, height - tabAreaInsets.top - tabAreaInsets.bottom);
                  width += tabExtent;
                  break;
              case TOP:
              case BOTTOM:
              default:
                  width = Math.max(width, calculateMaxTabWidth(tabPlacement));
                  tabExtent = preferredTabAreaHeight(tabPlacement, width - tabAreaInsets.left - tabAreaInsets.right);
                  height += tabExtent;
            }
            
            if (tabPane.getTabCount() == 0) {
            	switch(tabPlacement) {
                	case LEFT:
                	case RIGHT:
                		width = 3;
                		height = 64;
                		break;
                    case TOP:
                    case BOTTOM:
                    default:
                    	height = 16;
                    	width = 16;
            	}
            }
            
            return new Dimension(width + insets.left + insets.right + contentInsets.left + contentInsets.right, 
                             height + insets.bottom + insets.top + contentInsets.top + contentInsets.bottom);

        }
    }
    
    @Override
    protected void paintTabArea (final Graphics graphics, final int tabPlacement, 
    		final int selectedIndex) {
    	super.paintTabArea(graphics, tabPlacement, selectedIndex);
    	
    	if (tabPane.getTabCount() == 0) {
    		graphics.setFont (labelFont);
    		graphics.setColor (textColour);
    		graphics.drawString (emptyText, 3, 12);
    	}
    }
}