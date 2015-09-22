package example.multiview;

import io.db.Connect;
import io.db.ConnectFactory;
import io.db.FormatResultSet;
import io.json.JSONStructureMaker;
import io.parcoord.db.MakeTableModel;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import model.graph.Edge;
import model.graph.EdgeSetValueMaker;
import model.graph.GraphFilter;
import model.graph.GraphModel;
import model.graph.impl.SymmetricGraphInstance;
import model.matrix.DefaultMatrixTableModel;
import model.matrix.MatrixTableModel;
import model.shared.selection.LinkedGraphMatrixSelectionModelBridge;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.MissingNode;
import org.codehaus.jackson.node.ObjectNode;

import swingPlus.graph.GraphCellRenderer;
import swingPlus.graph.JGraph;
import swingPlus.graph.force.impl.BarnesHut2DForceCalculator;
import swingPlus.graph.force.impl.EdgeWeightedAttractor;
import swingPlus.matrix.JHeaderRenderer;
import swingPlus.matrix.JMatrix;
import swingPlus.parcoord.JColumnList;
import swingPlus.parcoord.JColumnList2;
import swingPlus.parcoord.JParCoord;
import swingPlus.shared.MyFrame;
import swingPlus.tablelist.ColumnSortControl;
import swingPlus.tablelist.JEditableVarColTable;
import ui.StackedRowTableUI;
import util.Messages;
import util.colour.ColorUtilities;
import util.ui.NewMetalTheme;
import util.ui.VerticalLabelUI;
import example.graph.renderers.node.NodeDegreeGraphCellRenderer;
import example.multiview.renderers.edge.EdgeCountFatEdgeRenderer;
import example.multiview.renderers.matrix.JSONObjHeaderRenderer;
import example.multiview.renderers.matrix.KeyedDataHeaderRenderer;
import example.multiview.renderers.matrix.NumberShadeRenderer;
import example.multiview.renderers.node.JSONNodeTypeGraphRenderer;
import example.multiview.renderers.node.JSONTooltipGraphCellRenderer;
import example.multiview.renderers.node.KeyedDataGraphCellRenderer;
import example.multiview.renderers.node.TableTooltipGraphCellRenderer;
import example.multiview.renderers.node.valuemakers.NodeTotalEdgeWeightValueMaker;
import example.tablelist.renderers.ColourBarCellRenderer;


public class NapierDBVis {


	static final Logger LOGGER = Logger.getLogger (NapierDBVis.class);
	
	/**
	 * @param args
	 */
	public static void main (final String[] args) {	
		//final MetalLookAndFeel lf = new MetalLookAndFeel();
		MetalLookAndFeel.setCurrentTheme (new NewMetalTheme());
		PropertyConfigurator.configure (Messages.makeProperties ("log4j"));
		
		new NapierDBVis ();
	}


	public NapierDBVis () {	
		
		TableModel tableModel = null;
		GraphModel graph = null;	
		TableModel listTableModel = null;
		
		MatrixTableModel matrixModel = null;
		Map<JsonNode, String> nodeTypeMap = null;
		
		final Properties connectionProperties = Messages.makeProperties ("dbconnect", this.getClass(), false);
		final Properties queryProperties = Messages.makeProperties ("queries", this.getClass(), false);
		final Connect connect = ConnectFactory.getConnect (connectionProperties);
		//ResultSet resultSet = null;
		
		Statement stmt;
		try {
			stmt = connect.getConnection().createStatement();
			
			
			//final ResultSet resultSet = stmt.executeQuery ("Select * from people where peopleid>0;");
			final String peopleDataQuery = queryProperties.get ("PeopleData").toString();
			System.err.println (peopleDataQuery);
			final ResultSet peopleDataResultSet = stmt.executeQuery (peopleDataQuery);
			final MakeTableModel mtm2 = new MakeTableModel();
			tableModel = mtm2.makeTable (peopleDataResultSet);
			
			//final ResultSet resultSet = stmt.executeQuery ("Select * from people where peopleid>0;");
			final String pubJoinQuery = queryProperties.get ("PublicationJoin").toString();
			System.err.println (pubJoinQuery);
			final ResultSet pubJoinResultSet = stmt.executeQuery (pubJoinQuery);
			//FormatResultSet.getInstance().printResultSet (resultSet);
			final MakeTableModel mtm = new MakeTableModel();
			TableModel tableModel2 = mtm.makeTable (pubJoinResultSet);
			

			//final DatabaseMetaData dmd = connect.getConnection().getMetaData();
			//final ResultSet resultSet2 = dmd.getProcedures (connect.getConnection().getCatalog(), null, "%");
			//FormatResultSet.getInstance().printResultSet (resultSet2);		
			final String pubsByYearQuery = queryProperties.get ("PubsByYear").toString();
			System.err.println (pubsByYearQuery);
			final ResultSet pubsByYearResultSet = stmt.executeQuery (pubsByYearQuery);
			final MakeTableModel mtm3 = new MakeTableModel();
			TableModel tableModel3 = mtm3.makeTable (pubsByYearResultSet);
			listTableModel = makePubByYearTable (tableModel3);
			
			
			
			Map<Object, KeyedData> keyDataMap = makeKeyedDataMap (tableModel, 0, 1);
			graph = makeGraph (keyDataMap, "peopleid", tableModel2);
			matrixModel = new DefaultMatrixTableModel (graph);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			connect.close();
		}
		connect.close();
		
		System.err.println (tableModel == null ? "no model" : "tableModel rows: "+tableModel.getRowCount()+", cols: "+tableModel.getColumnCount());
		/*


		
		try {
			final ObjectMapper objMapper = new ObjectMapper ();
			final JsonNode rootNode = objMapper.readValue (new File (fileName), JsonNode.class);
			LOGGER.info ("rootnode: "+rootNode);
			
			final JSONStructureMaker structureMaker = new JSONStructureMaker (rootNode);
			graph = structureMaker.makeGraph (new String[] {"people"}, new String[] {"publications", "grants"});
			//graph = structureMaker.makeGraph (new String[] {"grants"}, new String[] {"publications", "people"});
			//graph = structureMaker.makeGraph (new String[] {"publications", "people", "grants"}, new String[] {"people"});	
			
			//tableModel = structureMaker.makeTable ("publications");
			tableModel = structureMaker.makeTable ("people");
			
			
			matrixModel = new DefaultMatrixTableModel (graph);
			
			
			nodeTypeMap = structureMaker.makeNodeTypeMap (new String[] {"publications", "people", "grants"});
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		*/
		
		Map<Object, Integer> keyRowMap = makeKeyRowMap (tableModel, 0);

		
		final JGraph jgraph = new JGraph (graph);
		final EdgeWeightedAttractor edgeWeighter = new EdgeWeightedAttractor ();
		jgraph.setAttractiveForceCalculator (edgeWeighter);
		jgraph.setShowEdges (true);

		final EdgeSetValueMaker weightedEdgeMaker = new NodeTotalEdgeWeightValueMaker ();
		
		//final GraphCellRenderer tableTupleRenderer = new TableTupleGraphRenderer (tableModel, keyRowMap);
		final GraphCellRenderer jsonGraphRenderer = new JSONNodeTypeGraphRenderer (nodeTypeMap);
		jgraph.setDefaultNodeRenderer (String.class, new NodeDegreeGraphCellRenderer (10.0));
		jgraph.setDefaultNodeRenderer (JsonNode.class, jsonGraphRenderer);
		jgraph.setDefaultNodeRenderer (ObjectNode.class, jsonGraphRenderer);
		jgraph.setDefaultNodeRenderer (KeyedData.class, new KeyedDataGraphCellRenderer (weightedEdgeMaker));
		jgraph.setDefaultEdgeRenderer (Integer.class, new EdgeCountFatEdgeRenderer ());
		jgraph.setDefaultNodeToolTipRenderer (KeyedData.class, new TableTooltipGraphCellRenderer ());
		
		
		
		final JTable pubTable = new JEditableVarColTable (listTableModel);
		//final JTable jtable3 = new JTable (dtm);
		pubTable.setSelectionMode (ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		pubTable.setRowSelectionAllowed (true);
		
		//jt2.setColumnSelectionAllowed (true);
		pubTable.setRowSorter (new TableRowSorter<DefaultTableModel> ((DefaultTableModel)listTableModel));
		final StackedRowTableUI tlui = new StackedRowTableUI ();
		pubTable.setUI (tlui);
		tlui.setRelativeLayout (true);
		final Color[] columnColours = new Color [pubTable.getColumnCount() - 1];
		for (int n = 0; n < columnColours.length; n++) {
			double perc = (double)n / columnColours.length;
			columnColours[n] = ColorUtilities.mixColours (Color.orange, new Color (0, 128, 255), (float)perc);
		}
		pubTable.getTableHeader().setReorderingAllowed(true);
		pubTable.getTableHeader().setResizingAllowed(false);
		System.err.println ("ptc: "+pubTable.getColumnModel().getColumnCount());
		for (int col = 1; col < pubTable.getColumnCount(); col++) {
			System.err.println ("col: "+col+", ptyc: "+pubTable.getColumnModel().getColumn(col));
			pubTable.getColumnModel().getColumn(col).setCellRenderer (new ColourBarCellRenderer (columnColours [(col - 1) % columnColours.length]));
		}
		
		final JColumnList jcl = new JColumnList (pubTable) {
			@Override
			public boolean isCellEditable (final int row, final int column) {
				return super.isCellEditable (row, column) && row > 0;
			}
		};
		//jcl.addTable (pubTable);
		
		
		
		
		final JMatrix jmatrix = new JMatrix ((TableModel) matrixModel);
		//final JHeaderRenderer stringHeader = new JSONObjHeaderRenderer ();
		//final JHeaderRenderer stringHeader2 = new JSONObjHeaderRenderer ();
		final JHeaderRenderer stringHeader = new KeyedDataHeaderRenderer ();
		final JHeaderRenderer stringHeader2 = new KeyedDataHeaderRenderer ();
		jmatrix.getRowHeader().setDefaultRenderer (Object.class, stringHeader);
		jmatrix.getRowHeader().setDefaultRenderer (String.class, stringHeader);
		jmatrix.getColumnHeader().setDefaultRenderer (Object.class, stringHeader2);
		jmatrix.getColumnHeader().setDefaultRenderer (String.class, stringHeader2);
		((JLabel)stringHeader2).setUI (new VerticalLabelUI (false));
		
		stringHeader.setSelectionBackground (jmatrix.getRowHeader());
		stringHeader2.setSelectionBackground (jmatrix.getColumnHeader());
		//jmatrix.setDefaultRenderer (HashSet.class, stringHeader);
		jmatrix.setDefaultRenderer (String.class, stringHeader);
		jmatrix.setDefaultRenderer (Integer.class, new NumberShadeRenderer ());
		
		
		
		
		final JTable table = new JParCoord (tableModel);
		table.setSelectionMode (ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setRowSelectionAllowed (true);
		table.setAutoCreateRowSorter (true);
		table.setColumnSelectionAllowed (true);
		table.setForeground (Color.lightGray);
		table.setSelectionForeground (Color.orange);
		if (table instanceof JParCoord) {
			((JParCoord)table).setBrushForegroundColour (Color.gray);
			((JParCoord)table).setBrushSelectionColour (Color.red);
			((JParCoord)table).setSelectedStroke (new BasicStroke (2.0f));
			//((JParCoord)table).setBrushing (true);
		}
		table.setGridColor (Color.gray);
		table.setShowVerticalLines (false);
		table.setBorder (BorderFactory.createEmptyBorder (24, 2, 24, 2));
		if (table.getRowSorter() instanceof TableRowSorter) {
			final TableRowSorter<? extends TableModel> trs = (TableRowSorter<? extends TableModel>)table.getRowSorter();
		}
		table.setAutoResizeMode (JTable.AUTO_RESIZE_OFF);

		/*
		jgraph.setPreferredSize (new Dimension (768, 640));
		table.setPreferredSize (new Dimension (768, 384));
		table.setMinimumSize (new Dimension (256, 128));
		

		
		final LinkedGraphMatrixSelectionModelBridge selectionBridge = new LinkedGraphMatrixSelectionModelBridge ();
		selectionBridge.addJGraph (jgraph);
		selectionBridge.addJTable (table);
		selectionBridge.addJTable (jmatrix);
		*/
		
		SwingUtilities.invokeLater (
			new Runnable () {
				
				@Override
				public void run() {						
					
					final JFrame jf2 = new MyFrame ("JGraph Demo");
					jf2.setSize (1024, 768);
					
					
					
					final JPanel optionPanel = new JPanel ();
					optionPanel.setLayout (new BoxLayout (optionPanel, BoxLayout.Y_AXIS));
					
					
					final JSlider llengthSlider = new JSlider (20, 1000, (int)edgeWeighter.getLinkLength());
					llengthSlider.addChangeListener(
						new ChangeListener () {
							@Override
							public void stateChanged (final ChangeEvent cEvent) {
								edgeWeighter.setLinkLength (llengthSlider.getValue());
							}
						}
					);
					
					final JSlider lstiffSlider = new JSlider (20, 1000, edgeWeighter.getStiffness());
					lstiffSlider.addChangeListener(
						new ChangeListener () {
							@Override
							public void stateChanged (final ChangeEvent cEvent) {
								edgeWeighter.setStiffness (lstiffSlider.getValue());
							}
						}
					);
					
					
					final JSlider repulseSlider = new JSlider (1, 50, 10);
					repulseSlider.addChangeListener(
						new ChangeListener () {
							@Override
							public void stateChanged (final ChangeEvent cEvent) {
								((BarnesHut2DForceCalculator)jgraph.getRepulsiveForceCalculator()).setAttenuator (3.0 / repulseSlider.getValue());
							}
						}
					);
					
					
					final JCheckBox showSingletons = new JCheckBox ("Show singletons", true);
					showSingletons.addActionListener (
						new ActionListener () {
							@Override
							public void actionPerformed (final ActionEvent e) {
								final Object source = e.getSource();
								if (source instanceof JCheckBox) {
									final boolean selected = ((JCheckBox)source).isSelected();
									
									final GraphFilter singletonFilter = new GraphFilter () {

										@Override
										public boolean includeNode (final Object obj) {
											return jgraph.getModel().getEdges(obj).size() > 0 || selected;
										}

										@Override
										public boolean includeEdge (final Edge edge) {
											return true;
										}
									};
									
									jgraph.setGraphFilter (singletonFilter);
								}

							}
						}
					);
					
					final JButton clearSelections = new JButton ("Clear Selections");
					clearSelections.addActionListener (
						new ActionListener () {
							@Override
							public void actionPerformed (ActionEvent e) {
								jgraph.getSelectionModel().clearSelection ();
							}
						}
					);
					
					
					final JButton graphFreezer = new JButton ("Freeze Graph");
					graphFreezer.addActionListener (
						new ActionListener () {
							@Override
							public void actionPerformed (ActionEvent e) {
								jgraph.pauseWorker();
							}
						}
					);

					
					optionPanel.add (new JLabel ("Link Length:"));
					optionPanel.add (llengthSlider);
					optionPanel.add (new JLabel ("Link Stiffness:"));
					optionPanel.add (lstiffSlider);
					optionPanel.add (new JLabel ("Repulse Strength:"));
					optionPanel.add (repulseSlider);
					optionPanel.add (showSingletons);
					optionPanel.add (clearSelections);
					optionPanel.add (graphFreezer);
					
					JPanel listTablePanel = new JPanel (new BorderLayout ());
					listTablePanel.add (new JScrollPane (pubTable), BorderLayout.CENTER);
					
					final Box pubControlPanel = Box.createVerticalBox();
					final JScrollPane pubTableScrollPane = new JScrollPane (pubControlPanel);
					pubTableScrollPane.setPreferredSize (new Dimension (168, 400));
					jcl.getColumnModel().getColumn(1).setWidth (30);
					listTablePanel.add (pubTableScrollPane, BorderLayout.WEST);
					
					JTable columnSorter = new ColumnSortControl (pubTable);
					pubControlPanel.add (jcl.getTableHeader());
					pubControlPanel.add (jcl);
					pubControlPanel.add (columnSorter.getTableHeader());
					pubControlPanel.add (columnSorter);
					
					JScrollPane parCoordsScrollPane = new JScrollPane (table);
					JScrollPane matrixScrollPane = new JScrollPane (jmatrix);
					JTabbedPane jtp = new JTabbedPane ();
					
					JPanel graphPanel = new JPanel (new BorderLayout ());
					graphPanel.add (jgraph, BorderLayout.CENTER);
					graphPanel.add (optionPanel, BorderLayout.WEST);
					
					jtp.addTab ("Node-Link", graphPanel);
					jtp.addTab ("Matrix", matrixScrollPane);
					jtp.addTab ("Pubs", listTablePanel);
					jtp.addTab ("||-Coords", parCoordsScrollPane);
					jtp.setPreferredSize(new Dimension (800, 480));
					
					//jf2.getContentPane().add (optionPanel, BorderLayout.EAST);
					jf2.getContentPane().add (jtp, BorderLayout.CENTER);
					//jf2.getContentPane().add (tableScrollPane, BorderLayout.SOUTH);
					
					jf2.setVisible (true);
				}
			}
		);
	}
	
	public GraphModel makeGraph (final ResultSet nodeSet, final ResultSet edgeSet) throws SQLException {
		
		edgeSet.beforeFirst();
		final GraphModel graph  = new SymmetricGraphInstance ();	
		
		// Look through the rootnode for fields named 'nodeType'
		// Add that nodeTypes' subfields as nodes to a graph
		while (edgeSet.next()) {
			Object author1 = edgeSet.getObject(1);
			Object author2 = edgeSet.getObject(2);
			graph.addNode (author1);
			graph.addNode (author2);
			final Set<Edge> edges = graph.getEdges (author1, author2);
			if (edges.isEmpty()) {
				graph.addEdge (author1, author2, Integer.valueOf (1));
			} else {
				final Iterator<Edge> edgeIter = edges.iterator();
				final Edge firstEdge = edgeIter.next();
				final Integer val = (Integer)firstEdge.getEdgeObject();
				firstEdge.setEdgeObject (Integer.valueOf (val.intValue() + 1));
				//graph.removeEdge (firstEdge);
				//graph.addEdge (node1, node2, Integer.valueOf (val.intValue() + 1));
			}
		}
		
		return graph;
	}
	
	
	
	
	public GraphModel makeGraph (final TableModel nodes, final String primaryKeyColumn, final TableModel edges) throws SQLException {
		
		final GraphModel graph  = new SymmetricGraphInstance ();	
		
		final Map<Object, Integer> primaryKeyRowMap = new HashMap<Object, Integer> ();
		for (int row = 0; row < nodes.getRowCount(); row++) {
			primaryKeyRowMap.put (nodes.getValueAt (row, 0), Integer.valueOf (row));
		}
		
		
		// Look through the rootnode for fields named 'nodeType'
		// Add that nodeTypes' subfields as nodes to a graph
		for (int row = 0; row < edges.getRowCount(); row++) {
			Object authorKey1 = edges.getValueAt (row, 0);
			Object authorKey2 = edges.getValueAt (row, 1);
			int authorIndex1 = (primaryKeyRowMap.get(authorKey1) == null ? -1 : primaryKeyRowMap.get(authorKey1).intValue());
			int authorIndex2 = (primaryKeyRowMap.get(authorKey2) == null ? -1 : primaryKeyRowMap.get(authorKey2).intValue());
			
			if (authorIndex1 >= 0 && authorIndex2 >= 0) {
				Object graphNode1 = nodes.getValueAt (authorIndex1, 1);
				Object graphNode2 = nodes.getValueAt (authorIndex2, 1);
				
				graph.addNode (graphNode1);
				graph.addNode (graphNode2);
				final Set<Edge> gedges = graph.getEdges (graphNode1, graphNode2);
				if (gedges.isEmpty()) {
					graph.addEdge (graphNode1, graphNode2, Integer.valueOf (1));
				} else {
					final Iterator<Edge> edgeIter = gedges.iterator();
					final Edge firstEdge = edgeIter.next();
					final Integer val = (Integer)firstEdge.getEdgeObject();
					firstEdge.setEdgeObject (Integer.valueOf (val.intValue() + 1));
				}
			}
		}
		
		return graph;
	}
	
	
	
	public GraphModel makeGraph (final Map<Object, KeyedData> keyDataMap, final String primaryKeyColumn, final TableModel edges) throws SQLException {
		
		final GraphModel graph  = new SymmetricGraphInstance ();		
		
		// Look through the rootnode for fields named 'nodeType'
		// Add that nodeTypes' subfields as nodes to a graph
		for (int row = 0; row < edges.getRowCount(); row++) {
			Object authorKey1 = edges.getValueAt (row, 0);
			Object authorKey2 = edges.getValueAt (row, 1);
			
			if (authorKey1 != null && authorKey2 != null) {
				Object graphNode1 = keyDataMap.get (authorKey1);
				Object graphNode2 = keyDataMap.get (authorKey2);
				
				if (graphNode1 != null && graphNode2 != null) {
					graph.addNode (graphNode1);
					graph.addNode (graphNode2);
	
					final Set<Edge> gedges = graph.getEdges (graphNode1, graphNode2);
					if (gedges.isEmpty()) {
						graph.addEdge (graphNode1, graphNode2, Integer.valueOf (1));
					} else {
						final Iterator<Edge> edgeIter = gedges.iterator();
						final Edge firstEdge = edgeIter.next();
						final Integer val = (Integer)firstEdge.getEdgeObject();
						firstEdge.setEdgeObject (Integer.valueOf (val.intValue() + 1));
					}
				}
			}
		}
		
		return graph;
	}
	
	public Map<Object, Integer> makeKeyRowMap (final TableModel tableModel, final int columnPKIndex) {
		final Map<Object, Integer> primaryKeyRowMap = new HashMap<Object, Integer> ();
		for (int row = 0; row < tableModel.getRowCount(); row++) {
			primaryKeyRowMap.put (tableModel.getValueAt (row, 0), Integer.valueOf (row));
		}
		return primaryKeyRowMap;
	}
	
	
	public Map<Object, KeyedData> makeKeyedDataMap (final TableModel tableModel, final int columnPKIndex, final int columnLabelIndex) {
		final Map<Object, KeyedData> primaryKeyDataMap = new HashMap<Object, KeyedData> ();
		for (int row = 0; row < tableModel.getRowCount(); row++) {
			primaryKeyDataMap.put (tableModel.getValueAt (row, columnPKIndex), makeKeyedData (tableModel, columnPKIndex, columnLabelIndex, row));
		}
		return primaryKeyDataMap;
	}
	
	
	public KeyedData makeKeyedData (final TableModel tableModel, final int columnPKIndex, final int columnLabelIndex, final int rowIndex) {
		List<Object> data = new ArrayList<Object> ();
		for (int n = 0; n < tableModel.getColumnCount(); n++) {
			data.add (tableModel.getValueAt (rowIndex, n));
		}
		KeyedData kd = new KeyedData (tableModel.getValueAt (rowIndex, columnPKIndex), data, columnLabelIndex);
		return kd;
	}
	
	/**
	 * can't do pivot queries in ANSI SQL
	 * @param sqlresult
	 * @return
	 */
	public TableModel makePubByYearTable (final TableModel sqlresult) {
		DefaultTableModel tm = new DefaultTableModel () {
			
		    public Class<?> getColumnClass(int columnIndex) {
		    	if (columnIndex > 0) {
		    		return Long.class;
		    	}
		    	return Integer.class;
		    }
		    
		    public boolean isCellEditable (final int row, final int column) {
		    	return false;
		    }
		};
		
		Map<Object, List<Long>> yearsToTypes = new HashMap<Object, List<Long>> ();
		Map<Object, Integer> columnTypes = new HashMap<Object, Integer> ();
		
		tm.addColumn ("Year");
		
		int col = 1;
		for (int sqlrow = 0; sqlrow < sqlresult.getRowCount(); sqlrow++) {
			Object type = sqlresult.getValueAt (sqlrow, 1);
			if (columnTypes.get(type) == null) {
				columnTypes.put(type, Integer.valueOf(col));
				tm.addColumn (type);
				col++;
			}
		}
		System.err.println ("cols: "+columnTypes+", "+columnTypes.size());
		
		for (int sqlrow = 0; sqlrow < sqlresult.getRowCount(); sqlrow++) {
			Object year = sqlresult.getValueAt (sqlrow, 0);
			if (year != null) {
				Object type = sqlresult.getValueAt (sqlrow, 1);
				Object val = sqlresult.getValueAt (sqlrow, 2);
				int colIndex = columnTypes.get(type).intValue();
				List<Long> store = yearsToTypes.get (year);
				if (store == null) {
					Long[] storep = new Long [col - 1];
					Arrays.fill (storep, Long.valueOf(0));
					List<Long> longs = Arrays.asList (storep);
					store = new ArrayList (longs);
					//Collections.fill (store, Long.valueOf (0));
					yearsToTypes.put (year, store);
				}
				store.set (colIndex - 1, (Long)val);
			}
		}
		
		for (Entry<Object, List<Long>> yearEntry : yearsToTypes.entrySet()) {
			Object[] rowData = new Object [col];
			rowData[0] = yearEntry.getKey();
			for (int n = 1; n < col; n++) {
				rowData[n] = yearEntry.getValue().get(n-1);
			}
			tm.addRow(rowData);
		}
		
		return tm;
	}
}
