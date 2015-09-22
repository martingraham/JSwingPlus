package swingPlus.parcoord;

import java.util.BitSet;

public class RowCoordData {
	/**
	 * Convenience class to parcel up data objects and associated column x-coordinates for a table row
	 * @author cs22
	 */
	Object[] data;
	int[] xcoords;
	int[] ycoords;
	BitSet nullColumnFlags;
	boolean continuousPath = true;
	int activeColumns = 0;
	
	
	public RowCoordData (final Object[] data, final int[] xcoords, final int[] ycoords, final BitSet nullColumnFlags) {
		this.data = data;
		this.xcoords = xcoords;
		this.nullColumnFlags = nullColumnFlags;
		this.ycoords = ycoords;
	}


	public Object[] getData() {
		return data;
	}


	public int[] getXCoords() {
		return xcoords;
	}


	public int[] getYCoords() {
		return ycoords;
	}


	public BitSet getNullColumnFlags() {
		return nullColumnFlags;
	}


	public boolean isContinuousPath() {
		return continuousPath;
	}


	public int getActiveColumns() {
		return activeColumns;
	}


	public void setData(Object[] data) {
		this.data = data;
	}


	public void setXcoords(int[] xcoords) {
		this.xcoords = xcoords;
	}


	public void setYcoords(int[] ycoords) {
		this.ycoords = ycoords;
	}


	public void setNullColumnFlags(BitSet nullColumnFlags) {
		this.nullColumnFlags = nullColumnFlags;
	}


	public void setContinuousPath(boolean continuousPath) {
		this.continuousPath = continuousPath;
	}


	public void setActiveColumns(int activeColumns) {
		this.activeColumns = activeColumns;
	}
	
	
}
