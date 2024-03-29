/*
 *
 * This is a dialog for adding new Employees and saving records to file
 *
 * */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

import net.miginfocom.swing.MigLayout;

public class AddRecordDialog extends JDialog implements ActionListener {
	private JTextField idField, ppsField, surnameField, firstNameField, salaryField;
	private JComboBox<String> genderCombo, departmentCombo, fullTimeCombo;
	private JButton save, cancel;
	private final EmployeeDetails parent;
	private static final Color ERROR_COLOR = new Color(255, 150, 150);
	// constructor for add record dialog
	public AddRecordDialog(EmployeeDetails parent) {
		setTitle("Add Record");
		setModal(true);
		this.parent = parent;
		this.parent.setEnabled(false);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		JScrollPane scrollPane = new JScrollPane(dialogPane());
		setContentPane(scrollPane);

		getRootPane().setDefaultButton(save);

		setSize(500, 370);
		setLocation(350, 250);
		setVisible(true);
	}// end AddRecordDialog

	// initialize dialog container
	public Container dialogPane() {
		JPanel empDetails = new JPanel(new MigLayout());
		empDetails.setBorder(BorderFactory.createTitledBorder("Employee Details"));

		empDetails.add(createLabel("ID:"), "growx, pushx");
		empDetails.add(idField = createNonEditableTextField(), "growx, pushx, wrap");

		empDetails.add(createLabel("PPS Number:"), "growx, pushx");
		empDetails.add(ppsField = createTextField(new JTextFieldLimit(9)), "growx, pushx, wrap");

		empDetails.add(createLabel("Surname:"), "growx, pushx");
		empDetails.add(surnameField = createTextField(), "growx, pushx, wrap");

		empDetails.add(createLabel("First Name:"), "growx, pushx");
		empDetails.add(firstNameField = createTextField(), "growx, pushx, wrap");

		empDetails.add(createLabel("Gender:"), "growx, pushx");
		empDetails.add(genderCombo = createComboBox(this.parent.gender), "growx, pushx, wrap");

		empDetails.add(createLabel("Department:"), "growx, pushx");
		empDetails.add(departmentCombo = createComboBox(this.parent.department), "growx, pushx, wrap");

		empDetails.add(createLabel("Salary:"), "growx, pushx");
		empDetails.add(salaryField = createTextField(), "growx, pushx, wrap");

		empDetails.add(createLabel("Full Time:"), "growx, pushx");
		empDetails.add(fullTimeCombo = createComboBox(this.parent.fullTime), "growx, pushx, wrap");

		JPanel buttonPanel = createButtonPanel();
		empDetails.add(buttonPanel, "span 2,growx, pushx,wrap");
		idField.setText(Integer.toString(this.parent.getNextFreeId()));

		return empDetails;
	}

	private JLabel createLabel(String text) {
		return new JLabel(text);
	}

	private JTextField createTextField() {
		return new JTextField(20);
	}

	private JTextField createTextField(Document document) {
		JTextField field = new JTextField(20);
		field.setDocument(document);
		return field;
	}

	private JComboBox<String> createComboBox(String[] items) {
		return new JComboBox<>(items);
	}

	private JTextField createNonEditableTextField() {
		JTextField field = new JTextField(20);
		field.setEditable(false);
		return field;
	}

	private JPanel createButtonPanel() {
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(save = createButton("Save"));
		buttonPanel.add(cancel = createButton("Cancel"));
		return buttonPanel;
	}
	private JButton createButton(String text) {
		JButton button = new JButton(text);
		button.addActionListener(this);
		if ("Save".equals(text)) {
			button.requestFocus();
		}
		return button;
	}

	// add record to file
	public void addRecord() {
		// create new Employee record with details from text fields
		Employee theEmployee = new Employee(
				Integer.parseInt(idField.getText()),
				ppsField.getText().toUpperCase(),
				surnameField.getText().toUpperCase(),
				firstNameField.getText().toUpperCase(),
				getSelectedGender(),
				departmentCombo.getSelectedItem().toString(),
				Double.parseDouble(salaryField.getText()),
				isFullTime()
		);
		this.parent.currentEmployee = theEmployee;
		this.parent.addRecord(theEmployee);
		this.parent.displayRecords(theEmployee);
	}

	private Employee.Gender getSelectedGender() {
		String genderString = genderCombo.getSelectedItem().toString();
		return switch (genderString) {
			case "MALE" -> Employee.Gender.MALE;
			case "FEMALE" -> Employee.Gender.FEMALE;
			default -> Employee.Gender.OTHER;
		};
	}

	private boolean isFullTime() {
		return "Yes".equalsIgnoreCase((String) fullTimeCombo.getSelectedItem());
	}

	// check for input in text fields
	public boolean checkInput() {
		if (isFieldEmpty(ppsField) || isPpsInvalid() || isFieldEmpty(surnameField) ||
				isFieldEmpty(firstNameField) || isComboBoxNotSelected(genderCombo) ||
				isComboBoxNotSelected(departmentCombo) || isSalaryInvalid()) {
			JOptionPane.showMessageDialog(null, "Wrong values or format! Please check!");
			return false;
		}
		return true;
	}

	private boolean isFieldEmpty(JTextField field) {
		if (field.getText().trim().isEmpty()) {
			field.setBackground(ERROR_COLOR);
			return true;
		}
		field.setBackground(Color.WHITE);
		return false;
	}

	private boolean isPpsInvalid() {
		if (this.parent.correctPps(ppsField.getText().trim(), -1)) {
			ppsField.setBackground(ERROR_COLOR);
			return true;
		}
		ppsField.setBackground(Color.WHITE);
		return false;
	}

	private boolean isComboBoxNotSelected(JComboBox<String> comboBox) {
		if (comboBox.getSelectedIndex() == 0) {
			comboBox.setBackground(ERROR_COLOR);
			return true;
		}
		comboBox.setBackground(Color.WHITE);
		return false;
	}

	private boolean isSalaryInvalid() {
		try {
			double salary = Double.parseDouble(salaryField.getText());
			if (salary < 0) {
				salaryField.setBackground(ERROR_COLOR);
				return true;
			}
		} catch (NumberFormatException e) {
			salaryField.setBackground(ERROR_COLOR);
			return true;
		}
		salaryField.setBackground(Color.WHITE);
		return false;
	}

	// set text field to white colour
	public void setToWhite() {
		ppsField.setBackground(Color.WHITE);
		surnameField.setBackground(Color.WHITE);
		firstNameField.setBackground(Color.WHITE);
		salaryField.setBackground(Color.WHITE);
		genderCombo.setBackground(Color.WHITE);
		departmentCombo.setBackground(Color.WHITE);
		fullTimeCombo.setBackground(Color.WHITE);
	}// end setToWhite

	// action performed
	public void actionPerformed(ActionEvent e) {
		// if chosen option save, save record to file
		if (e.getSource() == save) {
			saveAction();
		} else if (e.getSource() == cancel) {
			cancelAction();
		}
	}// end actionPerformed

	private void saveAction() {
		if (checkInput()) {
			addRecord();
			dispose();
			this.parent.changesMade = true;
		} else {
			JOptionPane.showMessageDialog(null, "Wrong values or format! Please check!");
			setToWhite();
		}
	}

	private void cancelAction() {
		dispose();
	}
}// end class AddRecordDialog
