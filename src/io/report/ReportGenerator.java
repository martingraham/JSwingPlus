package io.report;

import io.TableModelWriter;

import java.util.List;
import java.util.Properties;

import javax.swing.JComponent;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableModel;



public interface ReportGenerator {

	void setTemplate (String urlString);
	
	void setProperties (Properties properties);
	
	
	void setSaveFilePath (String saveFilePath);
	
	
	TableModelWriter getTableWriter();

	void setTableWriter (TableModelWriter tableWriter);
	
	
	void setTableData (TableModel data, ListSelectionModel rowSelectionModel, ListSelectionModel columnSelectionModel);
	
	
	List<JComponent> getComponentsToAppend ();
	
	void setComponentsToAppend (List<JComponent> components);
	
	
	void populateTemplate ();
}
