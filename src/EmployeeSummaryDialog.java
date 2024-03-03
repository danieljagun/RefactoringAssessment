/*
 * 
 * This is the summary dialog for displaying all Employee details
 * 
 * */

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.util.*;

import net.miginfocom.swing.MigLayout;

public class EmployeeSummaryDialog extends JDialog implements ActionListener {
	// vector with all Employees details
	Vector<Object> allEmployees;
	JButton back;
	
	public EmployeeSummaryDialog(Vector<Object> allEmployees) {
		setTitle("Employee Summary");
		setModal(true);
		this.allEmployees = allEmployees;

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		JScrollPane scrollPane = new JScrollPane(summaryPane());
		setContentPane(scrollPane);

		setSize(850, 500);
		setLocation(350, 250);
		setVisible(true);

	}
	// initialise container
	private Container summaryPane() {
		String[] headerName = {"ID", "PPS Number", "Surname", "First Name", "Gender", "Department", "Salary", "Full Time"};
		int[] colWidth = {15, 100, 120, 120, 50, 120, 80, 80};

		JPanel summaryDialog = new JPanel(new BorderLayout());
		DefaultTableModel tableModel = new DefaultTableModel(allEmployees, new Vector<String>(Arrays.asList(headerName))) {
			Class[] columnTypes = new Class[]{Integer.class, String.class, String.class, String.class, Character.class, String.class, Double.class, Boolean.class};
			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
		};
		JTable employeeTable = new JTable(tableModel);
		configureTable(employeeTable, colWidth);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		back.addActionListener(this);
		buttonPanel.add(back);

		summaryDialog.add(buttonPanel, BorderLayout.SOUTH);
		summaryDialog.add(new JScrollPane(employeeTable), BorderLayout.CENTER);
		return summaryDialog;
	}// end summaryPane

	private void configureTable(JTable table, int[] colWidth) {
		for (int i = 0; i < table.getColumnCount(); i++) {
			TableColumn column = table.getColumnModel().getColumn(i);
			column.setMinWidth(colWidth[i]);
			if (i == 6) { // Salary column
				column.setCellRenderer(new DecimalFormatRenderer());
			} else if (i == 0) { // ID column
				column.setCellRenderer(new DefaultTableCellRenderer());
			}
		}
		table.setAutoCreateRowSorter(true);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == back){
			dispose();
		}

	}

	static class DecimalFormatRenderer extends DefaultTableCellRenderer {
		 private static final DecimalFormat format = new DecimalFormat(
		 "\u20ac ###,###,##0.00" );

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			setText(format.format(value));
			setHorizontalAlignment(JLabel.RIGHT);
			return this;
		}// end getTableCellRendererComponent
	}// DefaultTableCellRenderer
}// end class EmployeeSummaryDialog
