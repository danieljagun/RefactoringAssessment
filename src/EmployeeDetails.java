
/* * 
 * This is a menu driven system that will allow users to define a data structure representing a collection of 
 * records that can be displayed both by means of a dialog that can be scrolled through and by means of a table
 * to give an overall view of the collection contents.
 * 
 * */

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.text.*;
import java.util.*;
import java.util.function.*;


public class EmployeeDetails extends JFrame implements ActionListener, ItemListener, DocumentListener, WindowListener {
	// decimal format for inactive currency text field
	private static final DecimalFormat format = new DecimalFormat("\u20ac ###,###,##0.00");
	// decimal format for active currency text field
	private static final DecimalFormat fieldFormat = new DecimalFormat("0.00");
	// hold object start position in file
	private long currentByteStart = 0;
	private RandomFile application = new RandomFile();
	// display files in File Chooser only with extension .dat
	private FileNameExtensionFilter datfilter = new FileNameExtensionFilter("dat files (*.dat)", "dat");
	// hold file name and path for current file in use
	private File file;
	// holds true or false if any changes are made for text fields
	private boolean change = false;
	// holds true or false if any changes are made for file content
	boolean changesMade = false;
	private JMenuItem open, save, saveAs, create, modify, delete, firstItem, lastItem, nextItem, prevItem, searchById,
			searchBySurname, listAll, closeApp;
	private JButton first, previous, next, last, add, edit, deleteButton, displayAll, searchId, searchSurname,
			saveChange, cancelChange;
	private JComboBox<String> genderCombo, departmentCombo, fullTimeCombo;
	private JTextField idField, ppsField, surnameField, firstNameField, salaryField;
	private static EmployeeDetails frame = new EmployeeDetails();
	// font for labels, text fields and combo boxes
	Font font1 = new Font("SansSerif", Font.BOLD, 16);
	// holds automatically generated file name
	String generatedFileName;
	// holds current Employee object
	Employee currentEmployee;
	JTextField searchByIdField, searchBySurnameField;
	// gender combo box values
	String[] gender = { "", "M", "F" };
	// department combo box values
	String[] department = { "", "Administration", "Production", "Transport", "Management" };
	// full time combo box values
	String[] fullTime = { "", "Yes", "No" };

	private JMenuItem createMenuItem(String title, int mnemonic, KeyStroke accelerator, ActionListener listener) {
		JMenuItem item = new JMenuItem(title);
		item.setMnemonic(mnemonic);
		if (accelerator != null) {
			item.setAccelerator(accelerator);
		}
		item.addActionListener(listener);
		return item;
	}

	// initialize menu bar
	private JMenuBar menuBar() {
		JMenuBar menuBar = new JMenuBar();

		// Menu definitions
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		JMenu recordMenu = new JMenu("Records");
		recordMenu.setMnemonic(KeyEvent.VK_R);
		JMenu navigateMenu = new JMenu("Navigate");
		navigateMenu.setMnemonic(KeyEvent.VK_N);
		JMenu closeMenu = new JMenu("Exit");
		closeMenu.setMnemonic(KeyEvent.VK_E);

		menuBar.add(fileMenu);
		menuBar.add(recordMenu);
		menuBar.add(navigateMenu);
		menuBar.add(closeMenu);

		fileMenu.add(open = createMenuItem("Open", KeyEvent.VK_O, KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK), this));
		fileMenu.add(save = createMenuItem("Save", KeyEvent.VK_S, KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK), this));
		fileMenu.add(saveAs = createMenuItem("Save As", KeyEvent.VK_F2, KeyStroke.getKeyStroke(KeyEvent.VK_F2, ActionEvent.CTRL_MASK), this));

		recordMenu.add(create = createMenuItem("Create new Record", KeyEvent.VK_N, KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK), this));
		recordMenu.add(modify = createMenuItem("Modify Record", KeyEvent.VK_E, KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK), this));
		recordMenu.add(delete = createMenuItem("Delete Record", 0, null, this));

		navigateMenu.add(firstItem = createMenuItem("First", 0, null, this));
		navigateMenu.add(prevItem = createMenuItem("Previous", 0, null, this));
		navigateMenu.add(nextItem = createMenuItem("Next", 0, null, this));
		navigateMenu.add(lastItem = createMenuItem("Last", 0, null, this));
		navigateMenu.addSeparator();
		navigateMenu.add(searchById = createMenuItem("Search by ID", 0, null, this));
		navigateMenu.add(searchBySurname = createMenuItem("Search by Surname", 0, null, this));
		navigateMenu.add(listAll = createMenuItem("List all Records", 0, null, this));

		closeMenu.add(closeApp = createMenuItem("Close", KeyEvent.VK_F4, KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.CTRL_MASK), this));

		return menuBar;
	}// end menuBar

	// initialize search panel
	private JPanel searchPanel() {
		JPanel searchPanel = new JPanel(new MigLayout());
		searchPanel.setBorder(BorderFactory.createTitledBorder("Search"));

		addSearchField(searchPanel, "Search by ID:", searchByIdField = new JTextField(20), searchId = new JButton("Go"), "Search Employee By ID");
		addSearchField(searchPanel, "Search by Surname:", searchBySurnameField = new JTextField(20), searchSurname = new JButton("Go"), "Search Employee By Surname");

		return searchPanel;
	}// end searchPanel

	private void addSearchField(JPanel panel, String labelText, JTextField textField, JButton button, String buttonToolTip) {
		panel.add(new JLabel(labelText), "growx, pushx");
		panel.add(textField, "width 200:200:200, growx, pushx");
		textField.addActionListener(this);
		textField.setDocument(new JTextFieldLimit(20));
		panel.add(button, "width 35:35:35, height 20:20:20, growx, pushx, wrap");
		button.addActionListener(this);
		button.setToolTipText(buttonToolTip);
	}

	// initialize navigation panel
	private JPanel navigPanel() {
		JPanel navigPanel = new JPanel();
		navigPanel.setBorder(BorderFactory.createTitledBorder("Navigate"));

		navigPanel.add(createNavigationButton("first.png", "Display first Record"));
		navigPanel.add(createNavigationButton("prev.png", "Display next Record")); // Tooltip might be intended to say "Display previous Record"
		navigPanel.add(createNavigationButton("next.png", "Display previous Record"));
		navigPanel.add(createNavigationButton("last.png", "Display last Record"));

		return navigPanel;
	}// end naviPanel

	private JButton createNavigationButton(String iconFileName, String tooltip) {
		ImageIcon icon = new ImageIcon(new ImageIcon(iconFileName).getImage().getScaledInstance(17, 17, Image.SCALE_SMOOTH));
		JButton button = new JButton(icon);
		button.setPreferredSize(new Dimension(17, 17));
		button.addActionListener(this);
		button.setToolTipText(tooltip);
		return button;
	}

	private JPanel buttonPanel() {
		JPanel buttonPanel = new JPanel();

		buttonPanel.add(createButton("Add Record", "Add new Employee Record"), "growx, pushx");
		buttonPanel.add(createButton("Edit Record", "Edit current Employee"), "growx, pushx");
		buttonPanel.add(createButton("Delete Record", "Delete current Employee"), "growx, pushx, wrap");
		buttonPanel.add(createButton("List all Records", "List all Registered Employees"), "growx, pushx");

		return buttonPanel;
	}

	private JButton createButton(String text, String tooltip) {
		JButton button = new JButton(text);
		button.addActionListener(this);
		button.setToolTipText(tooltip);
		return button;
	}

	// initialize main/details panel
	private JPanel detailsPanel() {
		JPanel empDetails = new JPanel(new MigLayout());
		empDetails.setBorder(BorderFactory.createTitledBorder("Employee Details"));

		addTextField(empDetails, "ID:", idField, 20, false, null);
		addTextField(empDetails, "PPS Number:", ppsField, 20, true, new JTextFieldLimit(9));
		addTextField(empDetails, "Surname:", surnameField, 20, true, new JTextFieldLimit(20));
		addTextField(empDetails, "First Name:", firstNameField, 20, true, new JTextFieldLimit(20));
		addComboBox(empDetails, "Gender:", genderCombo, gender);
		addComboBox(empDetails, "Department:", departmentCombo, department);
		addTextField(empDetails, "Salary:", salaryField, 20, true, new JTextFieldLimit(20));
		addComboBox(empDetails, "Full Time:", fullTimeCombo, fullTime);

		JPanel buttonPanel = createButtonPanel();
		empDetails.add(buttonPanel, "span 2,growx, pushx,wrap");

		return empDetails;
	}// end detailsPanel

	private void addTextField(JPanel panel, String label, JTextField textField, int length, boolean editable, JTextFieldLimit limit) {
		panel.add(new JLabel(label), "growx, pushx");
		textField = new JTextField(length);
		textField.setEditable(editable);
		if (limit != null) {
			textField.setDocument(limit);
		}
		textField.getDocument().addDocumentListener(this);
		panel.add(textField, "growx, pushx, wrap");
	}

	private void addComboBox(JPanel panel, String label, JComboBox<String> comboBox, String[] items) {
		panel.add(new JLabel(label), "growx, pushx");
		comboBox = new JComboBox<>(items);
		comboBox.addItemListener(this);
		panel.add(comboBox, "growx, pushx, wrap");
	}

	private JPanel createButtonPanel() {
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(createButton("Save", "Save changes", false));
		buttonPanel.add(createButton("Cancel", "Cancel edit", false));
		return buttonPanel;
	}

	private JButton createButton(String text, String tooltip, boolean visible) {
		JButton button = new JButton(text);
		button.addActionListener(this);
		button.setToolTipText(tooltip);
		button.setVisible(visible);
		return button;
	}

	// display current Employee details
	public void displayRecords(Employee thisEmployee) {
		clearSearchFields();

		if (thisEmployee != null && thisEmployee.getEmployeeId() != 0) {
			idField.setText(Integer.toString(thisEmployee.getEmployeeId()));
			ppsField.setText(thisEmployee.getPps().trim());
			surnameField.setText(thisEmployee.getSurname().trim());
			firstNameField.setText(thisEmployee.getFirstName());
			genderCombo.setSelectedIndex(findIndexInArray(gender, thisEmployee.getGender().toString()));
			departmentCombo.setSelectedIndex(findIndexInArray(department, thisEmployee.getDepartment().trim()));
			salaryField.setText(format.format(thisEmployee.getSalary()));
			fullTimeCombo.setSelectedIndex(thisEmployee.getFullTime() ? 1 : 2);
		}

		change = false;
	}// end display records

	private void clearSearchFields() {
		searchByIdField.setText("");
		searchBySurnameField.setText("");
	}

	private int findIndexInArray(String[] array, String value) {
		for (int i = 0; i < array.length; i++) {
			if (value.equalsIgnoreCase(array[i])) {
				return i;
			}
		}
		return 0; // Default to first index if not found
	}

	private void displayDialog(Supplier<JDialog> dialogSupplier) {
		if (isSomeoneToDisplay()) {
			dialogSupplier.get();
		}
	}

	// display Employee summary dialog
	private void displayEmployeeSummaryDialog() {
		// display Employee summary dialog if these is someone to display
		if (isSomeoneToDisplay())
			new EmployeeSummaryDialog(getAllEmloyees());
	}// end displaySummaryDialog

	// display search by ID dialog
	private void displaySearchByIdDialog() {
		displayDialog(() -> new SearchByIdDialog(EmployeeDetails.this));
	}// end displaySearchByIdDialog

	// display search by surname dialog
	private void displaySearchBySurnameDialog() {
		if (isSomeoneToDisplay())
			new SearchBySurnameDialog(EmployeeDetails.this);
	}// end displaySearchBySurnameDialog

	private void navigateRecords(Function<Long, Long> navigateFunction) {
		if (isSomeoneToDisplay()) {
			application.openReadFile(file.getAbsolutePath());
			currentByteStart = navigateFunction.apply(currentByteStart);
			currentEmployee = application.readRecords(currentByteStart);
			while (currentEmployee.getEmployeeId() == 0) {
				currentByteStart = navigateFunction.apply(currentByteStart);
				currentEmployee = application.readRecords(currentByteStart);
			}
			application.closeReadFile();
		}
	}

	// find byte start in file for first active record
	private void firstRecord() {
		navigateRecords(start -> application.getFirst());
		if (currentEmployee.getEmployeeId() == 0) nextRecord();
	}// end firstRecord

	// find byte start in file for previous active record
	private void previousRecord() {
		navigateRecords(start -> application.getPrevious(start));
	}// end previousRecord

	// find byte start in file for next active record
	private void nextRecord() {
		navigateRecords(start -> application.getNext(start));
	}// end nextRecord

	// find byte start in file for last active record
	private void lastRecord() {
		navigateRecords(start -> application.getLast());
		if (currentEmployee.getEmployeeId() == 0) previousRecord();
	}// end lastRecord

	private void searchEmployee(Function<Employee, Boolean> searchCriteria) {
		boolean found = false;
		if (isSomeoneToDisplay()) {
			firstRecord();
			if (searchCriteria.apply(currentEmployee)) {
				found = true;
				displayRecords(currentEmployee);
			} else {
				int startId = currentEmployee.getEmployeeId();
				String startSurname = currentEmployee.getSurname().trim();
				nextRecord();
				while (!found && (currentEmployee.getEmployeeId() != startId || !currentEmployee.getSurname().trim().equalsIgnoreCase(startSurname))) {
					if (searchCriteria.apply(currentEmployee)) {
						found = true;
						displayRecords(currentEmployee);
						break;
					}
					nextRecord();
				}
			}
			if (!found) JOptionPane.showMessageDialog(null, "Employee not found!");
		}
	}

	// search by ID
	public void searchEmployeeById() {
		try {
			int idToSearch = Integer.parseInt(searchByIdField.getText().trim());
			searchEmployee(emp -> emp.getEmployeeId() == idToSearch);
		} catch (NumberFormatException e) {
			searchByIdField.setBackground(new Color(255, 150, 150));
			JOptionPane.showMessageDialog(null, "Wrong ID format!");
		} finally {
			searchByIdField.setBackground(Color.WHITE);
			searchByIdField.setText("");
		}
	}// end searchEmployeeByID

	// search Employee by surname
	public void searchEmployeeBySurname() {
		String surnameToSearch = searchBySurnameField.getText().trim();
		searchEmployee(emp -> emp.getSurname().trim().equalsIgnoreCase(surnameToSearch));
		searchBySurnameField.setText("");
	}// end searchEmployeeBySurname

	// get next free ID from Employees in the file
	public int getNextFreeId() {
		// Directly return 1 if the file is empty or no active records exist
		if (file.length() == 0 || !isSomeoneToDisplay()) {
			return 1;
		}

		// If there are active records, find the last one and increment its ID
		lastRecord(); // Finds the last active record
		return currentEmployee.getEmployeeId() + 1;
	}// end getNextFreeId

	// get values from text fields and create Employee object
	private Employee getChangedDetails() {
		boolean fullTime = "Yes".equalsIgnoreCase(fullTimeCombo.getSelectedItem().toString());
		String genderStr = genderCombo.getSelectedItem().toString();
		Employee.Gender gender = "M".equalsIgnoreCase(genderStr) ? Employee.Gender.MALE :
				"F".equalsIgnoreCase(genderStr) ? Employee.Gender.FEMALE : Employee.Gender.OTHER;

		return new Employee(
				Integer.parseInt(idField.getText()),
				ppsField.getText().toUpperCase(),
				surnameField.getText().toUpperCase(),
				firstNameField.getText().toUpperCase(),
				gender,
				departmentCombo.getSelectedItem().toString(),
				Double.parseDouble(salaryField.getText()),
				fullTime
		);
	}// end getChangedDetails

	private void performFileOperation(Runnable fileOperation) {
		application.openWriteFile(file.getAbsolutePath());
		fileOperation.run();
		application.closeWriteFile();
	}

	// add Employee object to fail
	public void addRecord(Employee newEmployee) {
		performFileOperation(() -> currentByteStart = application.addRecords(newEmployee));
	}// end addRecord

	// delete (make inactive - empty) record from file
	private void deleteRecord() {
		if (isSomeoneToDisplay()) {
			int returnVal = JOptionPane.showOptionDialog(frame, "Do you want to delete record?", "Delete",
					JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
			if (returnVal == JOptionPane.YES_OPTION) {
				performFileOperation(() -> application.deleteRecords(currentByteStart));
				if (isSomeoneToDisplay()) {
					nextRecord();
					displayRecords(currentEmployee);
				}
			}
		}
	}// end deleteRecord

	// create vector of vectors with all Employee details
	private Vector<Object> getAllEmloyees() {
		// vector of Employee objects
		Vector<Object> allEmployee = new Vector<Object>();
		Vector<Object> empDetails;// vector of each employee details
		long byteStart = currentByteStart;
		int firstId;

		firstRecord();// look for first record
		firstId = currentEmployee.getEmployeeId();
		// loop until all Employees are added to vector
		do {
			empDetails = new Vector<Object>();
			empDetails.addElement(new Integer(currentEmployee.getEmployeeId()));
			empDetails.addElement(currentEmployee.getPps());
			empDetails.addElement(currentEmployee.getSurname());
			empDetails.addElement(currentEmployee.getFirstName());
			empDetails.addElement(currentEmployee.getGender().toString());
			empDetails.addElement(currentEmployee.getDepartment());
			empDetails.addElement(new Double(currentEmployee.getSalary()));
			empDetails.addElement(new Boolean(currentEmployee.getFullTime()));

			allEmployee.addElement(empDetails);
			nextRecord();// look for next record
		} while (firstId != currentEmployee.getEmployeeId());// end do - while
		currentByteStart = byteStart;

		return allEmployee;
	}// end getAllEmployees

	// activate field for editing
	private void editDetails() {
		// activate field for editing if there is records to display
		if (isSomeoneToDisplay()) {
			// remove euro sign from salary text field
			salaryField.setText(fieldFormat.format(currentEmployee.getSalary()));
			change = false;
			setEnabled(true);// enable text fields for editing
		} // end if
	}// end editDetails

	// ignore changes and set text field unenabled
	private void cancelChange() {
		setEnabled(false);
		displayRecords(currentEmployee);
	}// end cancelChange

	// check if any of records in file is active - ID is not 0
	private boolean isSomeoneToDisplay() {
		boolean someoneToDisplay = false;
		// open file for reading
		application.openReadFile(file.getAbsolutePath());
		// check if any of records in file is active - ID is not 0
		someoneToDisplay = application.isSomeoneToDisplay();
		application.closeReadFile();// close file for reading
		// if no records found clear all text fields and display message
		if (!someoneToDisplay) {
			currentEmployee = null;
			idField.setText("");
			ppsField.setText("");
			surnameField.setText("");
			firstNameField.setText("");
			salaryField.setText("");
			genderCombo.setSelectedIndex(0);
			departmentCombo.setSelectedIndex(0);
			fullTimeCombo.setSelectedIndex(0);
			JOptionPane.showMessageDialog(null, "No Employees registered!");
		}
		return someoneToDisplay;
	}// end isSomeoneToDisplay

	// check for correct PPS format and look if PPS already in use
	public boolean correctPps(String pps, long currentByte) {
		if (pps.matches("\\d{7}[A-Z]{1,2}")) {
			application.openReadFile(file.getAbsolutePath());
			boolean ppsExist = application.isPpsExist(pps, currentByte);
			application.closeReadFile();
			return ppsExist;
		}
		return true; // Return true if PPS format is incorrect
	}// end correctPPS

	// check if file name has extension .dat
	private boolean checkFileName(File fileName) {
		return fileName.toString().endsWith(".dat");
	}// end checkFileName

	// check if any changes text field where made
	private boolean checkForChanges() {
		boolean anyChanges = false;
		// if changes where made, allow user to save there changes
		if (change) {
			saveChanges();// save changes
			anyChanges = true;
		} // end if
			// if no changes made, set text fields as unenabled and display
			// current Employee
		else {
			setEnabled(false);
			displayRecords(currentEmployee);
		} // end else

		return anyChanges;
	}// end checkForChanges

	// check for input in text fields
	private boolean checkInput() {
		boolean valid = true;
		// if any of inputs are in wrong format, colour text field and display
		// message
		if (ppsField.isEditable() && ppsField.getText().trim().isEmpty()) {
			ppsField.setBackground(new Color(255, 150, 150));
			valid = false;
		} // end if
		if (ppsField.isEditable() && correctPps(ppsField.getText().trim(), currentByteStart)) {
			ppsField.setBackground(new Color(255, 150, 150));
			valid = false;
		} // end if
		if (surnameField.isEditable() && surnameField.getText().trim().isEmpty()) {
			surnameField.setBackground(new Color(255, 150, 150));
			valid = false;
		} // end if
		if (firstNameField.isEditable() && firstNameField.getText().trim().isEmpty()) {
			firstNameField.setBackground(new Color(255, 150, 150));
			valid = false;
		} // end if
		if (genderCombo.getSelectedIndex() == 0 && genderCombo.isEnabled()) {
			genderCombo.setBackground(new Color(255, 150, 150));
			valid = false;
		} // end if
		if (departmentCombo.getSelectedIndex() == 0 && departmentCombo.isEnabled()) {
			departmentCombo.setBackground(new Color(255, 150, 150));
			valid = false;
		} // end if
		try {// try to get values from text field
			Double.parseDouble(salaryField.getText());
			// check if salary is greater than 0
			if (Double.parseDouble(salaryField.getText()) < 0) {
				salaryField.setBackground(new Color(255, 150, 150));
				valid = false;
			} // end if
		} // end try
		catch (NumberFormatException num) {
			if (salaryField.isEditable()) {
				salaryField.setBackground(new Color(255, 150, 150));
				valid = false;
			} // end if
		} // end catch
		if (fullTimeCombo.getSelectedIndex() == 0 && fullTimeCombo.isEnabled()) {
			fullTimeCombo.setBackground(new Color(255, 150, 150));
			valid = false;
		} // end if
			// display message if any input or format is wrong
		if (!valid)
			JOptionPane.showMessageDialog(null, "Wrong values or format! Please check!");
		// set text field to white colour if text fields are editable
		if (ppsField.isEditable())
			setToWhite();

		return valid;
	}

	// set text field background colour to white
	private void setToWhite() {
		ppsField.setBackground(UIManager.getColor("TextField.background"));
		surnameField.setBackground(UIManager.getColor("TextField.background"));
		firstNameField.setBackground(UIManager.getColor("TextField.background"));
		salaryField.setBackground(UIManager.getColor("TextField.background"));
		genderCombo.setBackground(UIManager.getColor("TextField.background"));
		departmentCombo.setBackground(UIManager.getColor("TextField.background"));
		fullTimeCombo.setBackground(UIManager.getColor("TextField.background"));
	}// end setToWhite

	// enable text fields for editing
	public void setEnabled(boolean booleanValue) {
		boolean search;
		if (booleanValue)
			search = false;
		else
			search = true;
		ppsField.setEditable(booleanValue);
		surnameField.setEditable(booleanValue);
		firstNameField.setEditable(booleanValue);
		genderCombo.setEnabled(booleanValue);
		departmentCombo.setEnabled(booleanValue);
		salaryField.setEditable(booleanValue);
		fullTimeCombo.setEnabled(booleanValue);
		saveChange.setVisible(booleanValue);
		cancelChange.setVisible(booleanValue);
		searchByIdField.setEnabled(search);
		searchBySurnameField.setEnabled(search);
		searchId.setEnabled(search);
		searchSurname.setEnabled(search);
	}// end setEnabled

	// open file
	private void openFile() {
		final JFileChooser fc = new JFileChooser();
		fc.setDialogTitle("Open");
		fc.setFileFilter(datfilter);

		promptToSaveChanges(); // Abstracted prompt logic into its own method

		int returnVal = fc.showOpenDialog(EmployeeDetails.this);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File newFile = fc.getSelectedFile();
			if (file.getName().equals(generatedFileName)) {
				file.delete();
			}
			file = newFile;
			application.openReadFile(file.getAbsolutePath());
			firstRecord();
			displayRecords(currentEmployee);
			application.closeReadFile();
		}
	}// end openFile

	private void promptToSaveChanges() {
		if (file.length() != 0 || change) {
			int returnVal = JOptionPane.showOptionDialog(frame, "Do you want to save changes?", "Save",
					JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
			if (returnVal == JOptionPane.YES_OPTION) {
				saveFile();
			}
		}
	}

	// save file
	private void saveFile() {
		// if file name is generated file name, save file as 'save as' else save
		// changes to file
		if (file.getName().equals(generatedFileName))
			saveFileAs();// save file as 'save as'
		else {
			// if changes has been made to text field offer user to save these
			// changes
			if (change) {
				int returnVal = JOptionPane.showOptionDialog(frame, "Do you want to save changes?", "Save",
						JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
				// save changes if user choose this option
				if (returnVal == JOptionPane.YES_OPTION) {
					// save changes if ID field is not empty
					if (!idField.getText().equals("")) {
						// open file for writing
						application.openWriteFile(file.getAbsolutePath());
						// get changes for current Employee
						currentEmployee = getChangedDetails();
						// write changes to file for corresponding Employee
						// record
						application.changeRecords(currentEmployee, currentByteStart);
						application.closeWriteFile();// close file for writing
					} // end if
				} // end if
			} // end if

			displayRecords(currentEmployee);
			setEnabled(false);
		} // end else
	}// end saveFile

	// save changes to current Employee
	private void saveChanges() {
		int returnVal = JOptionPane.showOptionDialog(frame, "Do you want to save changes to current Employee?", "Save",
				JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
		// if user choose to save changes, save changes
		if (returnVal == JOptionPane.YES_OPTION) {
			// open file for writing
			application.openWriteFile(file.getAbsolutePath());
			// get changes for current Employee
			currentEmployee = getChangedDetails();
			// write changes to file for corresponding Employee record
			application.changeRecords(currentEmployee, currentByteStart);
			application.closeWriteFile();// close file for writing
			changesMade = false;// state that all changes has bee saved
		} // end if
		displayRecords(currentEmployee);
		setEnabled(false);
	}// end saveChanges

	// save file as 'save as'
	private void saveFileAs() {
		final JFileChooser fc = new JFileChooser();
		File newFile;
		String defaultFileName = "new_Employee.dat";
		fc.setDialogTitle("Save As");
		// display files only with .dat extension
		fc.setFileFilter(datfilter);
		fc.setApproveButtonText("Save");
		fc.setSelectedFile(new File(defaultFileName));

		int returnVal = fc.showSaveDialog(EmployeeDetails.this);
		// if file has chosen or written, save old file in new file
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			newFile = fc.getSelectedFile();
			// check for file name
			if (!checkFileName(newFile)) {
				// add .dat extension if it was not there
				newFile = new File(newFile.getAbsolutePath() + ".dat");
				// create new file
				application.createFile(newFile.getAbsolutePath());
			} // end id
			else
				// create new file
				application.createFile(newFile.getAbsolutePath());

			try {// try to copy old file to new file
				Files.copy(file.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				// if old file name was generated file name, delete it
				if (file.getName().equals(generatedFileName))
					file.delete();// delete file
				file = newFile;// assign new file to file
			} // end try
			catch (IOException e) {
			} // end catch
		} // end if
		changesMade = false;
	}// end saveFileAs

	// allow to save changes to file when exiting the application
	private void exitApp() {
		if (file.length() != 0 && changesMade) {
			int returnVal = JOptionPane.showOptionDialog(frame, "Do you want to save changes?", "Save",
					JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
			if (returnVal == JOptionPane.YES_OPTION) {
				saveFile();
			}

			if (returnVal == JOptionPane.CANCEL_OPTION) {
				return;
			}
		}
		deleteGeneratedFileIfNeeded();
		System.exit(0);
	}// end exitApp

	private void deleteGeneratedFileIfNeeded() {
		if (file.getName().equals(generatedFileName)) {
			file.delete();
		}
	}

	// generate 20 character long file name
	private String getFileName() {
		String fileNameChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890_-";
		StringBuilder fileName = new StringBuilder(20);
		Random rnd = new Random();
		while (fileName.length() < 20) {
			int index = rnd.nextInt(fileNameChars.length());
			fileName.append(fileNameChars.charAt(index));
		}
		return fileName.toString();
	}// end getFileName

	// create file with generated file name when application is opened
	private void createRandomFile() {
		generatedFileName = getFileName() + ".dat";
		// assign generated file name to file
		file = new File(generatedFileName);
		// create file
		application.createFile(file.getName());
	}// end createRandomFile

	// action listener for buttons, text field and menu items
	public void actionPerformed(ActionEvent e) {
		if (requiresValidation(e.getSource()) && checkInput() && !checkForChanges()) {
			performAction(e.getSource());
		} else {
			performActionWithoutValidation(e.getSource());
		}
	}// end actionPerformed

	private boolean requiresValidation(Object source) {
		return source == closeApp || source == open || source == save || source == saveAs ||
				source == searchById || source == searchBySurname || source == firstItem || source == first ||
				source == prevItem || source == previous || source == nextItem || source == next ||
				source == lastItem || source == last || source == listAll || source == displayAll ||
				source == create || source == add || source == modify || source == edit || source == delete ||
				source == deleteButton;
	}

	private void performAction(Object source) {
		if (source == save) {
			saveFile();
			change = false;
		} else if (source == saveAs) {
			saveFileAs();
			change = false;
		} else if (source == open) {
			openFile();
		} else if (source == closeApp) {
			exitApp();
		}
	}

	private void performActionWithoutValidation(Object source) {
		if (source == searchId || source == searchByIdField) {
			searchEmployeeById();
		} else if (source == searchSurname || source == searchBySurnameField) {
			searchEmployeeBySurname();
		} else if (source == cancelChange) {
			cancelChange();
		}
	}

	// content pane for main dialog
	private void createContentPane() {
		setTitle("Employee Details");
		createRandomFile();// create random file name
		JPanel dialog = new JPanel(new MigLayout());

		setJMenuBar(menuBar());// add menu bar to frame
		// add search panel to frame
		dialog.add(searchPanel(), "width 400:400:400, growx, pushx");
		// add navigation panel to frame
		dialog.add(navigPanel(), "width 150:150:150, wrap");
		// add button panel to frame
		dialog.add(buttonPanel(), "growx, pushx, span 2,wrap");
		// add details panel to frame
		dialog.add(detailsPanel(), "gap top 30, gap left 150, center");

		JScrollPane scrollPane = new JScrollPane(dialog);
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		addWindowListener(this);
	}// end createContentPane

	// create and show main dialog
	private static void createAndShowGUI() {

		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.createContentPane();// add content pane to frame
		frame.setSize(760, 600);
		frame.setLocation(250, 200);
		frame.setVisible(true);
	}// end createAndShowGUI

	// main method
	public static void main(String args[]) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}// end main

	// DocumentListener methods
	public void changedUpdate(DocumentEvent d) {
		change = true;
		new JTextFieldLimit(20);
	}

	public void insertUpdate(DocumentEvent d) {
		change = true;
		new JTextFieldLimit(20);
	}

	public void removeUpdate(DocumentEvent d) {
		change = true;
		new JTextFieldLimit(20);
	}

	// ItemListener method
	public void itemStateChanged(ItemEvent e) {
		change = true;
	}

	// WindowsListener methods
	public void windowClosing(WindowEvent e) {
		// exit application
		exitApp();
	}

	public void windowActivated(WindowEvent e) {
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowDeactivated(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
	}
}// end class EmployeeDetails
