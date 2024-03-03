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
      return switch (genderChar) {
         case 'M' -> Gender.MALE;
         case 'F' -> Gender.FEMALE;
         default -> Gender.OTHER;
      };
   }

   // Read a record from specified RandomAccessFile
   public void read(RandomAccessFile file) throws IOException {
      setEmployeeId(file.readInt());
      setPps(readName(file));
      setSurname(readName(file));
      setFirstName(readName(file));
      setGender(charToGender(file.readChar()));
      setGender(charToGender(file.readChar()));
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
      return switch (gender) {
         case MALE -> 'M';
         case FEMALE -> 'F';
         default -> 'O';
      };
   }

   // Ensure that string is correct length
   private void writeName(RandomAccessFile file, String name) throws IOException {
      if (name == null) name = "";
      String formattedName = String.format("%-20s", name);
      file.writeChars(formattedName);
   }
}
