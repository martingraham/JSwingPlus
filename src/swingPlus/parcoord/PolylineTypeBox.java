package swingPlus.parcoord;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;

import swingPlus.parcoord.renderers.CurveRowRenderer;
import swingPlus.parcoord.renderers.DotRowRenderer;
import swingPlus.parcoord.renderers.PolylineRowRenderer;
import swingPlus.parcoord.renderers.RowRenderer;

public class PolylineTypeBox extends JComboBox implements ActionListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7007505168822165264L;
	static RowRenderer[] renderers = {new DotRowRenderer(), new PolylineRowRenderer (), new CurveRowRenderer ()};

	
	Collection<JParCoord> parCoordList;
	
	
	public PolylineTypeBox () {
		super (renderers);
		setSelectedIndex (-1);
		parCoordList = new ArrayList<JParCoord> ();
		addActionListener (this);
		this.setRenderer (
			new DefaultListCellRenderer () {
				/**
				 * 
				 */
				private static final long serialVersionUID = -7049033820606177874L;

				@Override
				public Component getListCellRendererComponent (final JList list,
						final Object value, final int index, final boolean isSelected,
						final boolean cellHasFocus) {
					super.getListCellRendererComponent (list, value, index, isSelected, cellHasFocus);
					if (value instanceof RowRenderer) {
						this.setText(((RowRenderer)value).getDescription());
					}
					return this;
				}
			}
		);
	}
	
	public void addToJPCList (final JParCoord jpc) {
		if (!parCoordList.contains (jpc)) {
			parCoordList.add (jpc);
		}
	}
	
	public boolean removeFromJPCList (final JComponent jComp) {
		return parCoordList.remove (jComp);
	}
	
	@Override
	public void actionPerformed (final ActionEvent aEvent) {
		final JComboBox jComboBox = (JComboBox)aEvent.getSource();
        final RowRenderer newRenderer = (RowRenderer)jComboBox.getSelectedItem();
        
        for (JParCoord jpc : parCoordList) {
        	jpc.setDefaultRenderer (newRenderer);
        }
	}	
}
