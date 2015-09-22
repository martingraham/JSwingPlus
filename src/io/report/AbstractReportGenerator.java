package io.report;

import io.TableModelWriter;

import java.util.List;
import java.util.Properties;

import javax.swing.JComponent;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableModel;



public abstract class AbstractReportGenerator implements ReportGenerator {

	Properties properties;
	TableModelWriter tableWriter;
	List<JComponent> componentsToAppend;
	String saveFilePath;
	
	
	
	public abstract void setTemplate (String urlString);
	
	public void setProperties (final Properties properties) {
		this.properties = properties;
	}
	
	@Override
	public void setSaveFilePath (final String saveFilePath) {
		this.saveFilePath = saveFilePath;
	}
	
	public final TableModelWriter getTableWriter() {
		return tableWriter;
	}

	public final void setTableWriter (final TableModelWriter tableWriter) {
		this.tableWriter = tableWriter;
	}
	
	public final void setTableData (final TableModel data, final ListSelectionModel rowSelectionModel, 
			final ListSelectionModel columnSelectionModel) {
		if (tableWriter != null) {
			tableWriter.setTableStructure (data, rowSelectionModel, columnSelectionModel);
		}
	}
	
	
	public final List<JComponent> getComponentsToAppend () {
		return componentsToAppend;
	}
	
	public final void setComponentsToAppend (final List<JComponent> components) {
		componentsToAppend = components;
	}
	
	abstract public void populateTemplate ();
}
