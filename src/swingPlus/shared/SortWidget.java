package swingPlus.shared;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionListener;

import model.shared.ArrayListModel;
import model.shared.MultiComparator;


import util.IconCache;
import util.Messages;
import util.collections.PropertyChangeArrayList;
import util.swing.DragDropList2;
import util.swing.JMultiIconButton;
import util.swing.MyDialog;

public class SortWidget extends JMultiIconButton {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2104041996986955021L;
	private static final Icon ICON = IconCache.makeIcon ("SortIcon");
	
	private List<Comparator<Object>> listValues;
	protected final DragDropList2 sortList;
	private transient final PropertyChangeListener pcl;
	private boolean oneExists = false;
	
	public SortWidget () {
		this ((List<Comparator<Object>>)null);
	}
	
	public SortWidget (final MultiComparator<?> multiComp) {
		this (multiComp.getComparatorList());
	}
	
	
	public SortWidget (final List<Comparator<Object>> listValues) {
		super (ICON);
		
		sortList = new DragDropList2 ();
		pcl = new PropertyChangeListener () {
			@Override
			public void propertyChange (final PropertyChangeEvent evt) {
				sortList.repaint ();
			}	
		};
		
		setSortListModel (listValues);
		
		this.addActionListener(
			new ActionListener () {
				
				@Override
				public void actionPerformed (final ActionEvent aEvent) {
					if (!oneExists && ((ArrayListModel)sortList.getModel()).getList() != null) { // only launch dialog if one isn't already open
						oneExists = true;
						final JDialog jdialog = new SortDialog (SortWidget.this, sortList);
						
						jdialog.addWindowListener (
							new WindowAdapter () {
								@Override
								public void windowClosed (final WindowEvent wEvent) {
									oneExists = false;
								}
							}
						);
					}
				}		
			}
		);
	}
	
	public final void addListSelectionListener (final ListSelectionListener lsl) {
		sortList.addListSelectionListener (lsl);
	}
	
	public final void removeListSelectionListener (final ListSelectionListener lsl) {
		sortList.removeListSelectionListener (lsl);
	}
	
	public final void setSortListModel (final List<Comparator<Object>> listValues) {
		if (this.listValues instanceof PropertyChangeArrayList<?>) {
			((PropertyChangeArrayList<?>)this.listValues).removePropertyChangeListener (pcl);
		}
		
		sortList.setModel (listValues);
		this.listValues = listValues;
		
		if (listValues instanceof PropertyChangeArrayList<?>) {
			((PropertyChangeArrayList<?>)listValues).addPropertyChangeListener (pcl);
		}
	}
	
	public JList getSortList () { return sortList; }
	
	
	
	static class SortDialog extends MyDialog {

		/**
		 * 
		 */
		private static final long serialVersionUID = -1329821344759484144L;
		
		 public SortDialog (final JButton origButton, final JList list) {
		
			super ((Frame)origButton.getTopLevelAncestor(), false);
			 
	        final JLabel jtitle = new JLabel (origButton.getToolTipText(), origButton.getIcon(), SwingConstants.LEFT);
	        jtitle.setAlignmentX (Component.CENTER_ALIGNMENT);
	        jtitle.setBorder (BorderFactory.createEmptyBorder (0, 0, 6, 0));
	        getUserPanel().add (jtitle);
			 
	        list.setMinimumSize (new Dimension (list.getPreferredSize().width, list.getFixedCellHeight() * Math.min (8, list.getModel().getSize())));
	        list.setToolTipText (Messages.getString (this.getClass(), "SortListTooltip"));

			getUserPanel().add (list);
			
			if (origButton.getClientProperty ("helpText") != null) {
				final JLabel helpText = new JLabel (origButton.getClientProperty ("helpText").toString());
				helpText.setAlignmentX (Component.CENTER_ALIGNMENT);
				getUserPanel().add (helpText);
			}
			makeVisible (null);        
		 }
	}
}
