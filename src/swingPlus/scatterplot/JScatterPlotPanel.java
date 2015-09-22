package swingPlus.scatterplot;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

import util.IconCache;
import util.MathUtils;
import util.Messages;
import util.ui.VerticalLabelUI;

import model.shared.SortedTableColumn;

public class JScatterPlotPanel extends JPanel implements PropertyChangeListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7570842319316416912L;
	private final static Logger LOGGER = Logger.getLogger (JScatterPlotPanel.class);
	
	private static final Icon SWAPICON = IconCache.makeIcon ("SwapAxesIcon");
	private static final Icon FLIPICON_H = IconCache.makeIcon ("FlipAxisIconH");
	private static final Icon FLIPICON_V = IconCache.makeIcon ("FlipAxisIconV");

	protected JPanel xPanel, yPanel;
	protected JLabel xLabel, yLabel;
	protected JScatterPlot sPlot;
	protected JSlider alphaSlider;
	protected Border labelBorder = BorderFactory.createMatteBorder (0, 0, 1, 1, 
			UIManager.getColor ("Button.background"));
	


	public JScatterPlotPanel () {
		this (null);
	}
	
	public JScatterPlotPanel (final JScatterPlot jsp) {
		super ();
		
		xLabel = new JLabel ("Horizontal", SwingConstants.CENTER);
		yLabel = new JLabel ("Vertical", SwingConstants.CENTER);
		xLabel.setBorder (labelBorder);
		yLabel.setBorder (labelBorder);
		yLabel.setUI (new VerticalLabelUI (false));
		final JButton swapButton = new SwapAxesButton ();
		final JButton flipXButton = new FlipAxisButton (SwingConstants.HORIZONTAL);
		final JButton flipYButton = new FlipAxisButton (SwingConstants.VERTICAL);
		alphaSlider = new JSlider (0, 255);
		alphaSlider.setToolTipText (Messages.getString ("alphaSlider.toolTipText"));
		xPanel = new JPanel (new GridLayout (2, 1));
		yPanel = new JPanel (new GridLayout (1, 2));

		swapButton.setPreferredSize (new Dimension (16, 16));
		flipXButton.setPreferredSize (new Dimension (16, 16));
		flipYButton.setPreferredSize (new Dimension (16, 16));
		setJsp (jsp);
		
		if (jsp != null && jsp.getSlaveTable() != null && jsp.getSlaveTable().getModel() != null) {
			pokeChange (jsp);
		}
		/*
		setLayout (new GridLayout (2,2));
		add (new SwapAxesButton ());
		add (xLabel);
		add (yLabel);
		add (jsp);
		*/
		alphaSlider.addChangeListener (
			new ChangeListener () {
				@Override
				public void stateChanged (final ChangeEvent event) {
					final JSlider slider = (JSlider)event.getSource();
					if (!slider.getValueIsAdjusting()) {
						final int val = slider.getValue ();
						sPlot.setAlpha ((int)(val * (255.0 / slider.getMaximum())));
						sPlot.resizeAndRepaint();
					}
				}
			}
		);
		
		final JPanel axesControlPanel = new JPanel (new GridLayout (2, 2));
		axesControlPanel.add (swapButton);
		axesControlPanel.add (flipXButton);
		axesControlPanel.add (flipYButton);
		axesControlPanel.add (new JLabel ());
		
		final JPanel panel2 = new JPanel ();
		panel2.setLayout (new BorderLayout ());
		panel2.add (axesControlPanel, BorderLayout.WEST);
		final JPanel panel3 = new JPanel (new GridLayout (2, 1));
		panel3.add (xLabel);
		panel3.add (xPanel);
		panel2.add (panel3, BorderLayout.CENTER);
		
		this.setLayout (new BorderLayout ());
		final JPanel panel4 = new JPanel (new GridLayout (1, 2));
		panel4.add (yLabel);
		panel4.add (yPanel);
		add (panel4, BorderLayout.WEST);
		add (panel2, BorderLayout.NORTH);
		add (jsp, BorderLayout.CENTER);
		add (alphaSlider, BorderLayout.SOUTH);
		this.addComponentListener (new SPPanelComponentListener ());
		
		setMinimumSize (new Dimension (128, 128));
	}
	
	public final void setJsp (final JScatterPlot newJsp) {
		if (sPlot != null) {
			sPlot.removePropertyChangeListener (this);
		}
		sPlot = newJsp;
		if (sPlot != null) {
			sPlot.addPropertyChangeListener (this);
		}
	}
	
	final void pokeChange (final JScatterPlot scatterPlot) {
		PropertyChangeEvent pce = new PropertyChangeEvent (scatterPlot, "xAxis", null, scatterPlot.getxAxis());
		propertyChange (pce);
		pce = new PropertyChangeEvent (scatterPlot, "yAxis", null, scatterPlot.getyAxis());
		propertyChange (pce);
	}
	
	public void propertyChange (final PropertyChangeEvent evt) {
		
		if (evt.getSource().equals (sPlot)) {
			if (evt.getPropertyName().equals ("xAxis")) {
				final SortedTableColumn<?> stc = (SortedTableColumn<?>)evt.getNewValue();
				if (stc != null) {
					constructGridLabels (xPanel, stc, xPanel.getWidth(), false);
					//xLabel.setText (constructHTMLTableLabel (stc, xLabel.getWidth()));
					xLabel.setText (stc.getHeaderValue().toString());
				}
			}
			else if (evt.getPropertyName().equals ("yAxis")) {
				final SortedTableColumn<?> stc = (SortedTableColumn<?>)evt.getNewValue();
				if (stc != null) {
					constructGridLabels (yPanel, stc, yPanel.getHeight(), true);
					//yLabel.setText (constructHTMLTableLabel (stc, yLabel.getHeight()));
					yLabel.setText (stc.getHeaderValue().toString());			
				}
			}
		}		
	}
	
	
	void constructGridLabels (final JPanel panel, final SortedTableColumn stc, final int width, final boolean vert) {
		
		panel.removeAll ();
		final VerticalLabelUI vui = new VerticalLabelUI (false);
		int discRange = stc.getDiscreteRange();
		int discMin = 0, discMax = discRange;
		final boolean sortOrderDesc = (stc.getCurrentOrder() == SortOrder.DESCENDING);
		
		if (discRange == 0) {
			panel.setLayout (new GridLayout (vert ? 2 : 1, vert ? 1 : 2));
			final JLabel label = new JLabel (stc.getMinBound().toString(), vert ^ sortOrderDesc ? SwingConstants.TRAILING : SwingConstants.LEADING);
			final JLabel label2 = new JLabel (stc.getMaxBound().toString(), vert ^ sortOrderDesc ? SwingConstants.LEADING : SwingConstants.TRAILING);
			if (vert) {
				label.setUI (vui);
				label2.setUI (vui);
			}
			panel.add (label);
			panel.add (label2);
		} else {
			final boolean intRange = stc.isA (Integer.class) || stc.isA (Short.class)
				|| stc.isA (Byte.class) || stc.isA (Long.class);

			if (intRange) {
				discMin = ((Number)stc.getMinBound()).intValue();
				discMax = ((Number)stc.getMaxBound()).intValue() + 1;
				discRange = discMax - discMin;
			}
			
			final int minUnitWidth = 32;
			int everyNthUnit = (width <= 0 ? discRange : (int)Math.floor ((double)discRange / (int)(width / minUnitWidth)));
			
			if (discRange < width) {
				final List<Integer> factors = MathUtils.getInstance().factorise (discRange);
				LOGGER.debug ("width: "+width+", no. of label slots: "+(int)(width / minUnitWidth)+", range: "+discRange+", everyNthUnit: "+everyNthUnit);
				if (everyNthUnit > 1) {
					final int index = Collections.binarySearch (factors, everyNthUnit);
					LOGGER.debug ("index: "+index+", factors: "+factors.toString());
					if (index > 0) {
						everyNthUnit = factors.get(index).intValue();
					} else {
						everyNthUnit = factors.get(-index - 1).intValue();
					}
				} else {
					everyNthUnit = 1;
				}
			}
			//else {
			//	
			//}
			
			final int labelCount = (int)(discRange / everyNthUnit);
			LOGGER.debug ("no of entries: "+labelCount+", discRange: "+discRange+", enu: "+everyNthUnit);
			
			panel.setLayout (new GridLayout (vert ? labelCount : 1,
					vert ? 1 : labelCount));
			int dVal = discMin;
			for (int labelNo = 0; labelNo < labelCount; labelNo++) {
				String text = null;
				if (intRange) {
					text = Integer.toString (dVal);
				} else {
					final int rowIndex = ((Integer)stc.getDiscreteList().get(dVal)).intValue();
					text = sPlot.getSlaveTable().getModel().getValueAt (rowIndex, stc.getModelIndex()).toString();
				}
				final JLabel label = new JLabel (text, SwingConstants.CENTER);
				if (vert) {
					label.setUI (vui);
				}
				panel.add (label);
				dVal += everyNthUnit;
			}
			addToolTips (panel, stc.getHeaderValue().toString());
		}
		
		if (sortOrderDesc) {
			reverseLabelOrder (panel);
		}
		
		panel.validate();
	}
	

	void addToolTips (final JPanel panel, final String columnName) {
		for (Component comp : panel.getComponents()) {
			if (comp instanceof JLabel) {
				final JLabel label = (JLabel)comp;
				label.setToolTipText (label.getText()+" ("+columnName+")");
			}
		}
	}
	
	void reverseLabelOrder (final JPanel panel) {
		final List<Component> compList = new ArrayList<Component> (panel.getComponentCount());
		for (int compIndex = 0; compIndex < panel.getComponentCount(); compIndex++) {
			compList.add (panel.getComponent (compIndex));
		}
		panel.removeAll ();
		for (int compIndex = compList.size(); --compIndex >= 0;) {
			panel.add (compList.get (compIndex));
		}
	}
	

	class SPPanelComponentListener extends ComponentAdapter {
		@Override
		public void componentResized (final ComponentEvent event) {
			PropertyChangeEvent pce = new PropertyChangeEvent (sPlot, "xAxis", null, sPlot.getxAxis());
			propertyChange (pce);
			pce = new PropertyChangeEvent (sPlot, "yAxis", null, sPlot.getyAxis());
			propertyChange (pce);
		}
	}
	
	
	class SwapAxesButton extends JButton {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4397777847680656103L;

		public SwapAxesButton () {
			
			super (SWAPICON);
			this.setToolTipText (Messages.getString ("swapButtonTooltipText"));

			addActionListener (
				new ActionListener () {
					@Override
					public void actionPerformed (final ActionEvent event) {
						if (sPlot != null) {
							final SortedTableColumn<?> stc = sPlot.getyAxis();
							sPlot.setyAxis (sPlot.getxAxis());
							sPlot.setxAxis (stc);
							sPlot.repaint ();
						}
					}		
				}
			);
		}
	}
	
	
	class FlipAxisButton extends JButton {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4397777847680656103L;


		
		public FlipAxisButton (final int orientation) {
			
			super (orientation == SwingUtilities.HORIZONTAL ? FLIPICON_H : FLIPICON_V);
			this.setToolTipText (Messages.getString ("flipButtonTooltipText"));

			addActionListener (
				new ActionListener () {
					@Override
					public void actionPerformed (final ActionEvent event) {
						if (sPlot != null) {
							if (orientation == SwingUtilities.HORIZONTAL) {
								flip (sPlot.getxAxis());
							}
							else if (orientation == SwingUtilities.VERTICAL) {
								flip (sPlot.getyAxis());
							}
							sPlot.repaint ();
						}
					}		
				}
			);
		}
		
		void flip (final SortedTableColumn<?> stc) {
			if (stc != null) {
				stc.flipSortOrder (true);
				pokeChange (sPlot);
			}
		}
	}
}
