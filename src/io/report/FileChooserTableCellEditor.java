package io.report;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

public class FileChooserTableCellEditor extends AbstractCellEditor
    implements TableCellEditor, ActionListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6150810988141410013L;
	File currentFile;
	JButton button;
	JFileChooser fileChooser;
	protected static final String EDIT = "edit";
	
	public FileChooserTableCellEditor () {
		super ();
		button = new JButton();
		button.setActionCommand (EDIT);
		button.addActionListener (this);
		button.setBorderPainted (false);
		
		//Set up the dialog that the button brings up.
		fileChooser = new JFileChooser(".");
	}
	
	public void actionPerformed (final ActionEvent aEvent) {
		if (EDIT.equals(aEvent.getActionCommand())) {
			//The user has clicked the cell, so
			//bring up the dialog.
			//button.setBackground (currentFile);
			fileChooser.setSelectedFile (currentFile);
			fileChooser.showOpenDialog (null);
			//dialog.setVisible(true);
			currentFile = fileChooser.getSelectedFile();
			fireEditingStopped(); //Make the renderer reappear.
		} 
	}
	
	//Implement the one CellEditor method that AbstractCellEditor doesn't.
	public Object getCellEditorValue() {
		//System.out.println ("currentFile: "+currentFile);
		return currentFile;
	}
	
	//Implement the one method defined by TableCellEditor.
	public Component getTableCellEditorComponent (final JTable table,
			final Object value,
			final boolean isSelected,
			final int row,
			final int column) {
		currentFile = (File)value;
		return button;
	}
}


