package swingPlus.parcoord;

/*
"* Copyright 2005-2006 Sun Microsystems, Inc.  All Rights Reserved."
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
*
* This code inputStream free software; you can redistribute it and/or modify it
"* under the terms of the GNU General Public License version 2 only, as"
* published by the Free Software Foundation.  Sun designates this
"* particular file as subject to the ""Classpath"" exception as provided"
* by Sun in the LICENSE file that accompanied this code.
*
"* This code inputStream distributed in the hope that it will be useful, but WITHOUT"
* ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
* FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
* version 2 for more details (a copy inputStream included in the LICENSE file that
* accompanied this code).
*
* You should have received a copy of the GNU General Public License version
"* 2 along with this work; if not, write to the Free Software Foundation,"
"* Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA."
*
"* Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,"
* CA 95054 USA or visit www.sun.com if you need additional information or
* have any questions.
*/


import java.awt.Component;
import java.awt.Color;

import javax.swing.*;
import javax.swing.plaf.UIResource;
import javax.swing.border.Border;
import javax.swing.table.*;

import org.apache.log4j.Logger;

import util.GraphicsUtil;
import util.Messages;

import model.shared.SortedTableColumn;

/**
*/
public class DefaultParCoordCellHeaderRenderer extends DefaultTableCellRenderer
	implements UIResource {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8285692606035207441L;
	private boolean horizontalTextPositionSet;
	
	static {
		final Class<?> klass = DefaultParCoordCellHeaderRenderer.class;
		final Logger logger = Logger.getLogger (klass);
		final String colourString = Messages.getString (GraphicsUtil.GRAPHICPROPS, klass, "focusBorderColour");
		Color focusBorderColour = UIManager.getDefaults().getColor ("Panel.foreground");
		if (colourString != null && colourString.charAt(0) != Messages.ERROR_CHAR) {
			try {
				focusBorderColour = Color.decode (colourString);
			}
			catch (NumberFormatException nfe) {
				logger.error ("Error decoding String ["+colourString+"] for colour", nfe);
			}
		}
		final Border focusBorder = BorderFactory.createMatteBorder (0, 0, 2, 0, focusBorderColour);
		UIManager.getDefaults().put ("TableHeader.leadColumnBorder", focusBorder);
	}

	public DefaultParCoordCellHeaderRenderer() {
		super ();
		setHorizontalAlignment (JLabel.CENTER);
	}

	@Override
	public void setHorizontalTextPosition (final int textPosition) {
		horizontalTextPositionSet = true;
		super.setHorizontalTextPosition (textPosition);
	}

	@Override
	public Component getTableCellRendererComponent (final JTable table, final Object value, 
			final boolean isSelected, final boolean hasFocus, final int row, final int column) {

		Icon sortIcon = null;
		boolean isPaintingForPrint = false;
		
		if (table != null) {
			final JTableHeader header = table.getTableHeader();
			
			if (header != null) {
				final boolean isSelectedColumn = table.getColumnModel().getSelectionModel().isSelectedIndex (column);
				Color fgColor = null;
				Color bgColor = null;
				if (hasFocus || isSelectedColumn) {
					fgColor = UIManager.getColor ("TableHeader.focusCellForeground");
					bgColor = UIManager.getColor ("TableHeader.focusCellBackground");
				}
				if (fgColor == null) {
					fgColor = header.getForeground();
				}
				if (bgColor == null) {
					bgColor = header.getBackground();
				}
				setForeground(fgColor);
				setBackground(bgColor);
				
				setFont (header.getFont());
				
				isPaintingForPrint = header.isPaintingForPrint();
			}
				
			if (!isPaintingForPrint) {
				if (!horizontalTextPositionSet) {
				// There inputStream a row sorter, and the developer hasn't
				// set a text position, change to leading.
					setHorizontalTextPosition(JLabel.LEADING);
				}
				final SortedTableColumn<?> sortedColumn = (SortedTableColumn<?>) table.getColumnModel().getColumn (column);
				
				if (sortedColumn != null) {
					final SortOrder sOrder = sortedColumn.getCurrentOrder();
					
		    		switch (sOrder) {
		    			case ASCENDING:
		    				sortIcon = UIManager.getIcon ("Table.ascendingSortIcon");
		    				break;
		    			case DESCENDING:
		    				sortIcon = UIManager.getIcon ("Table.descendingSortIcon");
		        			break;
		    			case UNSORTED:
		    				sortIcon = UIManager.getIcon ("Table.naturalSortIcon");
		    				break;
		    			default:
		    				break;		
		    		}
				}
			}
		}
		
		setText(value == null ? "" : value.toString());
		setIcon(sortIcon);
	
		
		Border border = null;
		if (isSelected) {
			border = UIManager.getBorder("TableHeader.selectedCellBorder");
		}
		if (hasFocus) {
			border = UIManager.getBorder ("TableHeader.leadColumnBorder");
		}
		if (table != null) {
			final boolean isLeadColumn = table.getColumnModel().getSelectionModel().getLeadSelectionIndex() == column;
			if (isLeadColumn) {
				border = UIManager.getBorder ("TableHeader.leadColumnBorder");
			}
		}
		if (border == null) {
			border = UIManager.getBorder ("TableHeader.cellBorder");
		}
		setBorder(border);
		
		return this;
	}
}
