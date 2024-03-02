import java.io.RandomAccessFile;
import java.io.IOException;

public class RandomAccessEmployeeRecord extends Employee {
   public static final int SIZE = 175; // Size of each RandomAccessEmployeeRecord object

   // Create empty record
   public RandomAccessEmployeeRecord() {
      super();
   }

   // Initialize record with details
   public RandomAccessEmployeeRecord(int employeeId, String pps, String surname, String firstName, char gender,
                                     String department, double salary, boolean fullTime) {
      super(employeeId, pps, surname, firstName, charToGender(gender), department, salary, fullTime);
   }

   // Convert char gender to Gender enum
   private static Gender charToGender(char genderChar) {
      switch (genderChar) {
         case 'M':
            return Gender.MALE;
         case 'F':
            return Gender.FEMALE;
         default:
            return Gender.OTHER;
      }
   }

   // Read a record from specified RandomAccessFile
   public void read(RandomAccessFile file) throws IOException {
      setEmployeeId(file.readInt());
      setPps(readName(file));
      setSurname(readName(file));
      setFirstName(readName(file));
      setGender(charToGender(file.readChar())); // Convert char to Gender enum
      setDepartment(readName(file));
      setSalary(file.readDouble());
      setFullTime(file.readBoolean());
   }

   // Ensure that string is correct length
   private String readName(RandomAccessFile file) throws IOException {
      char[] name = new char[20];
      for (int count = 0; count < name.length; count++) {
         name[count] = file.readChar();
      }
      return new String(name).replace('\0', ' ');
   }

   // Write a record to specified RandomAccessFile
   public void write(RandomAccessFile file) throws IOException {
      file.writeInt(getEmployeeId());
      writeName(file, getPps().toUpperCase());
      writeName(file, getSurname().toUpperCase());
      writeName(file, getFirstName().toUpperCase());
      file.writeChar(genderToChar(getGender()));
      writeName(file, getDepartment());
      file.writeDouble(getSalary());
      file.writeBoolean(getFullTime());
   }

   // Convert Gender enum to char gender
   private char genderToChar(Gender gender) {
      switch (gender) {
         case MALE:
            return 'M';
         case FEMALE:
            return 'F';
         default:
            return 'O';
      }
   }

   // Ensure that string is correct length
   private void writeName(RandomAccessFile file, String name) throws IOException {
      StringBuilder buffer = new StringBuilder();
      if (name != null) {
         buffer.append(name);
         int spacesToAdd = 20 - name.length();
         for (int i = 0; i < spacesToAdd; i++) {
            buffer.append(' ');
         }
      } else {
         for (int i = 0; i < 20; i++) {
            buffer.append(' ');
         }
      }
      file.writeChars(buffer.toString());
   }
}
