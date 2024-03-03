/*
 * 
 * This class is for accessing, creating and modifying records in a file
 * 
 * */

import javax.swing.*;
import java.io.IOException;
import java.io.RandomAccessFile;

public class RandomFile {
	private RandomAccessFile output;
	private RandomAccessFile input;

	// Create new file
	public void createFile(String fileName) {
		try (RandomAccessFile file = new RandomAccessFile(fileName, "rw")) {
			// File is created and can be immediately closed by try-with-resources
		} catch (IOException ioException) {
			JOptionPane.showMessageDialog(null, "Error processing file!");
			System.exit(1);
		}
	} // end createFile

	// Open file for adding or changing records
	public void openWriteFile(String fileName) {
		try // open file
		{
			output = new RandomAccessFile(fileName, "rw");
		} // end try
		catch (IOException ioException) {
			JOptionPane.showMessageDialog(null, "File does not exist!");
		} // end catch
	} // end method openFile

	// Close file for adding or changing records
	public void closeWriteFile() {
		if (output != null) {
			try {
				output.close();
			} catch (IOException ioException) {
				JOptionPane.showMessageDialog(null, "Error closing file!");
				// System.exit removed to allow the application to handle the error more gracefully
			}
		}
	} // end closeFile

	// Add records to file
// Add records to file
	public long addRecords(Employee employeeToAdd) {
		long currentRecordStart = 0;

		try {
			char genderChar = genderToChar(employeeToAdd.getGender()); // Simplified gender conversion

			RandomAccessEmployeeRecord record = new RandomAccessEmployeeRecord(employeeToAdd.getEmployeeId(), employeeToAdd.getPps(),
					employeeToAdd.getSurname(), employeeToAdd.getFirstName(), genderChar,
					employeeToAdd.getDepartment(), employeeToAdd.getSalary(), employeeToAdd.getFullTime());

			output.seek(output.length()); // Move to end of file
			record.write(output); // Write new record
			currentRecordStart = output.length() - RandomAccessEmployeeRecord.SIZE; // Calculate start of this record
		} catch (IOException ioException) {
			JOptionPane.showMessageDialog(null, "Error writing to file!");
		}

		return currentRecordStart;
	}

	private char genderToChar(Employee.Gender gender) {
		return switch (gender) {
			case MALE -> 'M';
			case FEMALE -> 'F';
			default -> 'O';
		};
	}

	// Change details for existing object
	public void changeRecords(Employee newDetails, long byteToStart) {
		try {
			char genderChar = genderToChar(newDetails.getGender()); // Use the refactored method

			RandomAccessEmployeeRecord record = new RandomAccessEmployeeRecord(newDetails.getEmployeeId(), newDetails.getPps(),
					newDetails.getSurname(), newDetails.getFirstName(), genderChar,
					newDetails.getDepartment(), newDetails.getSalary(), newDetails.getFullTime());

			output.seek(byteToStart); // Position to write
			record.write(output); // Write changes
		} catch (IOException ioException) {
			JOptionPane.showMessageDialog(null, "Error writing to file!");
		}
	}// end changeRecors

	// Delete existing object
	public void deleteRecords(long byteToStart) {
		try {
			RandomAccessEmployeeRecord record = new RandomAccessEmployeeRecord(); // Create empty object
			output.seek(byteToStart); // Look for proper position
			record.write(output); // Replace existing object with empty object
		} catch (IOException ioException) {
			JOptionPane.showMessageDialog(null, "Error writing to file!");
		}
	}// end deleteRecords

	// Open file for reading
	public void openReadFile(String fileName) {
		try // open file
		{
			input = new RandomAccessFile(fileName, "r");
		} // end try
		catch (IOException ioException) {
			JOptionPane.showMessageDialog(null, "File is not suported!");
		} // end catch
	} // end method openFile

	// Close file
	public void closeReadFile() {
		try // close file and exit
		{
			if (input != null)
				input.close();
		} // end try
		catch (IOException ioException) {
			JOptionPane.showMessageDialog(null, "Error closing file!");
			System.exit(1);
		} // end catch
	} // end method closeFile

	// Get position of first record in file
	public long getFirst() {
		try {
			return input.length() > 0 ? 0 : -1;
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Error accessing file!");
			return -1;
		}
	}// end getFirst

	// Get position of last record in file
	public long getLast() {
		long byteToStart = 0;

		try {// try to get position of last record
			byteToStart = input.length() - RandomAccessEmployeeRecord.SIZE;
		}// end try 
		catch (IOException e) {
		}// end catch

		return byteToStart;
	}// end getLast

	// Get position of next record in file
	public long getNext(long readFrom) {
		try {
			long fileLength = input.length();

			if (readFrom + RandomAccessEmployeeRecord.SIZE >= fileLength) {
				return 0;
			} else {
				return readFrom + RandomAccessEmployeeRecord.SIZE;
			}
		} catch (IOException e) {
			return -1;
		}
	}// end getNext

	// Get position of previous record in file
	public long getPrevious(long readFrom) {
		long byteToStart = readFrom;

		try {
			if (readFrom == 0) {
				byteToStart = input.length() - RandomAccessEmployeeRecord.SIZE;
			} else {
				byteToStart = readFrom - RandomAccessEmployeeRecord.SIZE;
			}
		} catch (IOException e) {

		}
		return byteToStart;
	}// end getPrevious

	// Get object from file in specified position
	public Employee readRecords(long byteToStart) {
		RandomAccessEmployeeRecord record = new RandomAccessEmployeeRecord();
		try {
			input.seek(byteToStart); // Seek to the specified position
			record.read(input); // Read the record
		} catch (IOException e) {
			// Log or handle the exception
			System.err.println("Error reading record from file: " + e.getMessage());
			return null;
		}
		return record;
	}// end readRecords

	// Check if PPS Number already in use
	public boolean isPpsExist(String pps, long currentByteStart) {
		RandomAccessEmployeeRecord record = new RandomAccessEmployeeRecord();
		boolean ppsExist = false;
		long currentByte = 0;

		try {// try to read from file and look for PPS Number
			// Start from start of file and loop until PPS Number is found or search returned to start position
			while (currentByte != input.length() && !ppsExist) {
				//if PPS Number is in position of current object - skip comparison
				if (currentByte != currentByteStart) {
					input.seek(currentByte);// Look for proper position in file
					record.read(input);// Get record from file
					// If PPS Number already exist in other record display message and stop search
					if (record.getPps().trim().equalsIgnoreCase(pps)) {
						ppsExist = true;
						JOptionPane.showMessageDialog(null, "PPS number already exist!");
					}// end if
				}// end if
				currentByte = currentByte + RandomAccessEmployeeRecord.SIZE;
			}// end while
		} // end try
		catch (IOException e) {
		}// end catch

		return ppsExist;
	}// end isPpsExist

	// Check if any record contains valid ID - greater than 0
	public boolean isSomeoneToDisplay() {
		boolean someoneToDisplay = false;
		long currentByte = 0;
		RandomAccessEmployeeRecord record = new RandomAccessEmployeeRecord();

		try {// try to read from file and look for ID
			// Start from start of file and loop until valid ID is found or search returned to start position
			while (currentByte != input.length() && !someoneToDisplay) {
				input.seek(currentByte);// Look for proper position in file
				record.read(input);// Get record from file
				// If valid ID exist in stop search
				if (record.getEmployeeId() > 0)
					someoneToDisplay = true;
				currentByte = currentByte + RandomAccessEmployeeRecord.SIZE;
			}// end while
		}// end try
		catch (IOException e) {
		}// end catch

		return someoneToDisplay;
	}// end isSomeoneToDisplay
}// end class RandomFile
