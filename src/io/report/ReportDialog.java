package io.report;

import io.LaunchSaveFileDialogBasic;
import io.property.PropertiesTable;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import model.graph.impl.EdgeInstance;
import model.shared.TableUtils;

import org.apache.log4j.Logger;


import swingPlus.matrix.ImageRenderer;
import swingPlus.parcoord.JParCoord;
import swingPlus.scatterplot.JScatterPlotPanel;
import swingPlus.shared.JTableST;
import util.GraphicsUtil;
import util.IconCache;
import util.Messages;
import util.screenshot.ScreenshotComponent;
import util.swing.MyDialog;

public class ReportDialog extends MyDialog {

	
    /**
	 * 
	 */
	private static final long serialVersionUID = -6251539165361209564L;
	static final Icon ICON = IconCache.makeIcon ("GenerateReportIcon");
	static final String TITLE = Messages.getString ("ReportDialog.title");
	static final Logger LOGGER = Logger.getLogger (ReportDialog.class);
	static final Border PROPERTY_PANEL_BORDER = BorderFactory.createCompoundBorder (
		BorderFactory.createEmptyBorder (0, 4, 4, 4), 
		BorderFactory.createMatteBorder (1, 0, 1, 0, UIManager.getDefaults().getColor ("Panel.background").darker())
	);
	static ReportDialog INSTANCE;
	
	private Properties properties;
	private JParCoord jpc;
	private Collection<JTable> tables;
	private JTable propertyEditingTable;
	private JTable imageTable;

	
	public static void showDialog (final Collection<Component> components) throws HeadlessException {

		final Iterator<Component> compIterator = components.iterator();
		final Component refComp = compIterator.next ();
        final Window window = SwingUtilities.getWindowAncestor (refComp);
        
        if (window instanceof Frame) {
        	if (INSTANCE == null) {
        		INSTANCE = new ReportDialog ((Frame)window, true, components, TITLE);
        		INSTANCE.setCloseAction (new ActionListener () {
                    public void actionPerformed (final ActionEvent aEvent) {
                    	if (aEvent.getSource() instanceof JComponent) {
            	        	final JComponent comp = (JComponent)aEvent.getSource();
            	        	final JDialog dialog = (JDialog)comp.getTopLevelAncestor();
            	            dialog.setVisible (false);
                    	}
                    }
                });
        	} else {
        		INSTANCE.refreshImageTable (); 
        		INSTANCE.pack ();
        	}
        	INSTANCE.makeVisible (null); // blocks until user brings dialog down...
        }
    } 

      
	public ReportDialog (final Frame frame, final boolean modal, final Collection<Component> components, final String title) {
		super (frame, modal);
		
		final Iterator<Component> compIterator = components.iterator();
		tables = new ArrayList<JTable> ();
		boolean parCoordFound = false;
		
		while (compIterator.hasNext()) {
			final Component comp = compIterator.next();
			if (comp instanceof JTable) {
				tables.add ((JTable)comp);
				
				if (!parCoordFound && comp instanceof JParCoord) {
					jpc = (JParCoord)comp;
					parCoordFound = true;
				}
			}
		}
		
		
		addOptions ();
		final JLabel jtitle = new JLabel (title, ICON, SwingConstants.LEFT);
        jtitle.setAlignmentX (Component.CENTER_ALIGNMENT);
        jtitle.setBorder (BorderFactory.createEmptyBorder (0, 0, 6, 0));
        getUserPanel().add (jtitle);
        
		properties = Messages.makeProperties ("io.report.defaultReport");
		properties.put ("Template File", new File("."));   
		//properties.put ("Save Report Here", new File(".")); 
        propertyEditingTable = new PropertiesTable (properties);
        propertyEditingTable.setFont (Font.decode (Messages.getString ("graphic", "systemFont")));
        propertyEditingTable.getColumnModel().getColumn(0).setCellRenderer (new KeyCellValueTypeRenderer ());
        propertyEditingTable.setDefaultEditor (File.class, new FileChooserTableCellEditor ());
        
        // Disable generate report button until all fields in property value column have some entry in them
        propertyEditingTable.getModel().addTableModelListener (
        	new TableModelListener () {
				@Override
				public void tableChanged (final TableModelEvent tmEvent) {
					final TableModel tModel = propertyEditingTable.getModel ();
					if (tmEvent.getType() == TableModelEvent.UPDATE && tmEvent.getFirstRow() != TableModelEvent.HEADER_ROW) {
						disableOptionsIfPropertiesIncomplete (tModel);
					}
				}
        	}
        );
        

        final JScrollPane tableScrollPane = new JScrollPane (propertyEditingTable);
        tableScrollPane.setPreferredSize (new Dimension (tableScrollPane.getPreferredSize().width, 200));
		
        getUserPanel().add (tableScrollPane);
        

        imageTable = new JTableST ();
        imageTable.setRowHeight (48);
        refreshImageTable ();
        final JScrollPane imageTableScrollPane = new JScrollPane (imageTable);
        imageTableScrollPane.setPreferredSize (new Dimension (imageTableScrollPane.getPreferredSize().width, 200));
        getUserPanel().add (imageTableScrollPane);
        
        disableOptionsIfPropertiesIncomplete (propertyEditingTable.getModel());
	}
	
	
	
	
	public final void addOptions () {
		
		this.getOptionBox().setLayout (new BoxLayout (this.getOptionBox(), BoxLayout.X_AXIS));
		
		final String[] buttonPrefixes = {"GenerateReport"};
		final ActionListener[] aListeners = {new ReportGeneratorListener ()}; 
		final JButton[] buttons = new JButton [buttonPrefixes.length];
		for (int index = 0; index < buttonPrefixes.length; index++) {
			final String buttonPrefix = buttonPrefixes [index];
			buttons[index] = new JButton (Messages.getString (buttonPrefix + "ActionName"));
			buttons[index].setToolTipText (Messages.getString (buttonPrefix + "ActionTooltip"));
			buttons[index].setIcon (IconCache.makeIcon (buttonPrefix + "Icon"));
			buttons[index].addActionListener (aListeners [index]);
			this.getOptionBox().add (Box.createHorizontalStrut (8));
			this.getOptionBox().add (buttons[index]);
			buttons[index].setEnabled (false);
		}
		
		closeButton.setMargin (null);
	}
	
	
	final void disableOptionsIfPropertiesIncomplete (final TableModel tModel) {
		boolean allPropertiesEntered = true;
		for (int viewRowIndex = 0; viewRowIndex < propertyEditingTable.getRowCount() && allPropertiesEntered; viewRowIndex++) {
			final int modelRowIndex = propertyEditingTable.convertRowIndexToModel (viewRowIndex);
			allPropertiesEntered &= (tModel.getValueAt(modelRowIndex, 1).toString().length() > 1);
		}
		
		final Component[] comps = getOptionBox().getComponents();
		for (Component comp : comps) {
			if (comp instanceof JButton && comp != closeButton) {
				final JButton button = (JButton)comp;
				button.setEnabled (allPropertiesEntered);
			}
		}
	}

	
	final TableModel refreshImageTable () {
		
		final ScreenshotComponent screenshotter = new ScreenshotComponent ();
        final Window window = SwingUtilities.getWindowAncestor (jpc);
        final List<JComponent> comps = GraphicsUtil.getComponentsBeneath ((JComponent)(((JFrame)window).getContentPane()), 
				(Class<?>)JParCoord.class, true);
        comps.addAll (GraphicsUtil.getComponentsBeneath ((JComponent)(((JFrame)window).getContentPane()), 
				(Class<?>)JScatterPlotPanel.class, true));
        
		final DefaultTableModel tModel = new DefaultTableModel () {
			/**
			 * 
			 */
			private static final long serialVersionUID = -5784412665270634785L;

			final Class<?>[] classArray = {JComponent.class, String.class, BufferedImage.class, Boolean.class};
			
			public Class<?> getColumnClass (final int columnIndex) {
				if (columnIndex >=0 && columnIndex < classArray.length) {
					return classArray [columnIndex];
				}
				return Object.class;
			}
		};
		
		final String[] imageTableColumnNames = new String [4];
		imageTableColumnNames[0] = "Comp Hidden";
		for (int imageCol = 1; imageCol < imageTableColumnNames.length; imageCol++) {
			imageTableColumnNames[imageCol] = Messages.getString ("ImageTableColumn"+imageCol);
		}
		tModel.setColumnIdentifiers (imageTableColumnNames);
		for (int index = 0; index < comps.size(); index++) {
			final BufferedImage image = screenshotter.captureComponentImage (comps.get(index), 0.25f);
			final Object[] rowData = new Object[] {comps.get(index), comps.get(index).getName(), new EdgeInstance (null, null, image), Boolean.TRUE};
			tModel.addRow (rowData);
		}
		
		imageTable.setModel (tModel);
		imageTable.removeColumn (imageTable.getColumnModel().getColumn(0));
		imageTable.setDefaultRenderer (BufferedImage.class, new ImageRenderer ());
		
		return tModel;
	}
	
	

	void generateReport (final ReportGenerator rGen) {
		rGen.setTemplate (properties.getProperty ("Template File"));
		rGen.setProperties (properties);
		final Collection<TableColumnModel> columnModels = new ArrayList<TableColumnModel> ();
		for (JTable table : tables) {
			columnModels.add (table.getColumnModel());
		}
		rGen.setTableData (jpc.getModel(), jpc.getUberSelection(), TableUtils.combineColumnModels (columnModels));
		final TableModel imageTableModel = imageTable.getModel ();
		final List<JComponent> comps = new ArrayList<JComponent> ();
		for (int row = 0; row < imageTableModel.getRowCount(); row++) {
			if (imageTableModel.getValueAt (row, 3) == Boolean.TRUE) {
				comps.add ((JComponent)imageTableModel.getValueAt (row, 0));
			}
		}
		rGen.setComponentsToAppend (comps);
		rGen.populateTemplate ();
	}
	
	

	
	class ReportGeneratorListener implements ActionListener {
		
		@Override
		public void actionPerformed (final ActionEvent aEvent) {
			if (propertyEditingTable.isEditing()) {
				propertyEditingTable.getCellEditor().stopCellEditing();
			}
			final String templateFile = properties.getProperty ("Template File");
			
			if (templateFile != null && templateFile.length() > 3) {
				LOGGER.debug ("TEMPLATE FILE: "+templateFile);
				final int suffixIndex = templateFile.lastIndexOf ('.');
				
				if (suffixIndex > 0) {
					final String suffix = templateFile.substring (suffixIndex);
					final String[] suffixArray = {suffix};
					final JFileChooser jfc = new LaunchSaveFileDialogBasic (suffixArray);
					final int state = jfc.showSaveDialog (ReportDialog.this);
					
					if (state == JFileChooser.APPROVE_OPTION) {

						final String filePath = jfc.getSelectedFile().getPath();
						
						if (templateFile.endsWith (".html")) {
							final HTMLReportGenerator rGen = new HTMLReportGenerator ();
							rGen.setSaveFilePath (filePath);
							generateReport (rGen);
							
							try {
								Desktop.getDesktop().browse (rGen.getHTMLFile().toURI());
							} catch (final IOException ioe) {
								LOGGER.error ("IOException attempting to launch default browser", ioe);
							}
							
							ReportDialog.this.closeButton.doClick ();
						}
						else if (templateFile.endsWith (".odt")) {
							final ODFReportGenerator rGen = new ODFReportGenerator ();
							rGen.setSaveFilePath (filePath);
							generateReport (rGen);
							
							ReportDialog.this.closeButton.doClick ();
						}
					}
				}
			}
		}
	}
	
	
	/**
	 * For each row, draws an icon in the key column of a property table
	 * which is dependent on the class of the object in the value column
	 * @author cs22
	 *
	 */
	static class KeyCellValueTypeRenderer extends DefaultTableCellRenderer {
	    /**
		 * 
		 */
		private static final long serialVersionUID = 6144817185675230102L;
		
		final Map<Class<?>, Icon> classIconMap = new HashMap <Class<?>, Icon> ();
		
		public KeyCellValueTypeRenderer() {
			super ();
			classIconMap.put (File.class, IconCache.makeIcon ("FolderIcon"));
			classIconMap.put (String.class, IconCache.makeIcon ("TextIcon"));
		}

		public Component getTableCellRendererComponent (final JTable table, final Object value,
				final boolean isSelected, final boolean hasFocus, final int row, final int column) {
			
			final int modelColumn = table.convertColumnIndexToModel (column);
			if (modelColumn == 0) {
				final Object pairValue = table.getValueAt (row, column == 0 ? 1 : 0);
				LOGGER.debug ("KeyCellValueTypeRenderer. pairValue: "+pairValue+", pairValueClass: "+(pairValue != null ? pairValue.getClass() : pairValue));
				if (pairValue != null) {
					Class<?> valueClass = pairValue.getClass();
					while (valueClass != Object.class && classIconMap.get (valueClass) == null) {
						valueClass = valueClass.getSuperclass();
					}
					this.setIcon (classIconMap.get (valueClass));
				}
			}
			return super.getTableCellRendererComponent (table, value, isSelected, 
					hasFocus, row, column);
		}
	}
}
