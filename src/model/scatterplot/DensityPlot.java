package model.scatterplot;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;

import model.shared.SortedTableColumn;

/**
 * Class that produces a 2D density plot when fed a JTable and two 
 * discretely-ordered SortedTableColumns (normally these columns are also
 * in the JTable). Selected and unselected plot layers are calculated.
 * Can also pass an optional further ListSelectionModel to allow 2 selection
 * states to be represented (i.e. a conjunctive selection) - this selection model
 * is indexed by model row order.
 * @author cs22
 *
 */
public class DensityPlot {

	final static Logger LOGGER = Logger.getLogger (DensityPlot.class);
	
	SortedTableColumn<?> xAxis, yAxis;
	JTable slaveTable;
	ListSelectionModel secondarySelectionModel;
	int[][][] densityPlot;
	
	public DensityPlot (final JTable slaveTable,
			final SortedTableColumn<?> xAxis, final SortedTableColumn<?> yAxis) {
		this (slaveTable, xAxis, yAxis, null);
	}
	
	public DensityPlot (final JTable slaveTable,
			final SortedTableColumn<?> xAxis, final SortedTableColumn<?> yAxis,
			final ListSelectionModel secondarySM) {
		this.xAxis = xAxis;
		this.yAxis = yAxis;
		this.slaveTable = slaveTable;
		this.secondarySelectionModel = secondarySM;
	}
	
	/**
	 * Calc that works out if the number of points in the density array is less
	 * than the number of rows in the table model. Usually best to find another
	 * way to count overplotting (i.e. directly drawing) if this isn't the case.
	 * @return
	 */	
	public double getEfficiency () {
		final int discRangeX = xAxis.getDiscreteRange();
		final int discRangeY = yAxis.getDiscreteRange();
		final int dXY = discRangeX * discRangeY;
		return (dXY == 0 ? 0.0 : (double)slaveTable.getRowCount() / dXY);
	}
	
	public int[][][] populate () {
		
		final int xRange = xAxis.getDiscreteRange();
		final int yRange = yAxis.getDiscreteRange();
		LOGGER.debug ("xAxis: "+xAxis.getHeaderValue().toString()+"\tr: "+xRange);
		LOGGER.debug ("yAxis: "+yAxis.getHeaderValue().toString()+"\tr: "+yRange);
		densityPlot = new int [xRange][yRange][3];
		
		final int rowMax = slaveTable.getRowCount();
		final TableModel tModel = slaveTable.getModel();
		final int xIndex = xAxis.getModelIndex ();
		final int yIndex = yAxis.getModelIndex ();
		final ListSelectionModel primarySelectionModel = slaveTable.getSelectionModel ();
		
		for (int row = rowMax; --row >= 0;) {
			final int modelRow = slaveTable.convertRowIndexToModel (row);
			final Object objx = tModel.getValueAt (modelRow, xIndex);
			final Object objy = tModel.getValueAt (modelRow, yIndex);
			
			if (objx != null && objy != null) {
				final int x = getY (xAxis, objx, xRange);
				final int y = getY (yAxis, objy, yRange);

				if (y >= 0 && x >= 0 && x < densityPlot.length && y < densityPlot[x].length) {
					final int selected = (secondarySelectionModel != null && secondarySelectionModel.isSelectedIndex (modelRow))
						? 2 : (primarySelectionModel.isSelectedIndex (row) ? 1 : 0);
					densityPlot[x][y][selected]++;
				}
			}
		}
	
		return densityPlot;
	}
	
    int getY (final SortedTableColumn<?> stc, final Object obj, final int range) {
    	final int y = stc.getY (obj, range - 1);
    	//if (!stc.getCurrentOrder().equals (SortOrder.ASCENDING)) {
		//	y = (range - 1) - y;
		//}
    	return y;
    }
	
	public int[][][] getDensityPlot () {
		return densityPlot;
	}
	
	@Override
	public String toString () {
		final StringBuilder sb = new StringBuilder ();
		sb.append ("X: "+xAxis.getHeaderValue().toString());
		sb.append ("\tY: "+yAxis.getHeaderValue().toString()+"\n");
		
		for (int row = 0; row < densityPlot[0].length; row++) {
			for (int col = 0; col < densityPlot.length; col++) {
				sb.append (densityPlot[col][row][0]+"\t");
			}
			sb.append ("\n");
		}
		
		sb.append ("\nselect\n");
		for (int row = 0; row < densityPlot[0].length; row++) {
			for (int col = 0; col < densityPlot.length; col++) {
				sb.append (densityPlot[col][row][1]+"\t");
			}
			sb.append ("\n");
		}
		
		sb.append ("\nuber select\n");
		for (int row = 0; row < densityPlot[0].length; row++) {
			for (int col = 0; col < densityPlot.length; col++) {
				sb.append (densityPlot[col][row][2]+"\t");
			}
			sb.append ("\n");
		}
		
		return sb.toString ();
	}
}
