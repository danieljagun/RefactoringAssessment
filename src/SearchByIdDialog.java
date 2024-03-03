/*
 *
 * This is the dialog for Employee search by ID
 *
 * */

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SearchByIdDialog extends JDialog implements ActionListener {
	private final EmployeeDetails parent;
	private final JTextField searchField = new JTextField(20);
	private final JButton search = new JButton("Search");
	private final JButton cancel = new JButton("Cancel");
	// constructor for SearchByIdDialog
	public SearchByIdDialog(EmployeeDetails parent) {
		setTitle("Search by ID");
		setModal(true);
		this.parent = parent;
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		setLayout(new BorderLayout());
		add(createSearchPanel(), BorderLayout.CENTER);

		pack();
		setLocationRelativeTo(parent);
		setVisible(true);
	}

	// initialize search container
	private JPanel createSearchPanel() {
		JPanel panel = new JPanel(new BorderLayout(5, 5));
		panel.add(new JLabel("Enter ID:"), BorderLayout.WEST);
		panel.add(searchField, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		search.addActionListener(this);
		cancel.addActionListener(this);
		buttonPanel.add(search);
		buttonPanel.add(cancel);

		panel.add(buttonPanel, BorderLayout.SOUTH);
		return panel;
	}// end searchPane

	// action listener for save and cancel button
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == search) {
			performSearch();
		} else if (e.getSource() == cancel) {
			dispose();
		}
	}// end actionPerformed

	private void performSearch() {
		try {
			int id = Integer.parseInt(searchField.getText());
			parent.searchByIdField.setText(String.valueOf(id));
			parent.searchEmployeeById();
			dispose();
		} catch (NumberFormatException ex) {
			searchField.setBackground(new Color(255, 150, 150));
			JOptionPane.showMessageDialog(this, "Wrong ID format!", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
}// end class searchByIdDialog
