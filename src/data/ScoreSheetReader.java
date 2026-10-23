package data;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Scanner;
import model.Student;

// Reads the students from the score sheet CSV, e.g.
// OrgDefinedId,Username,Last Name,First Name,Email,...
// #01400001,#ping.lee.2023,_,PING LEE,ping.lee.2023@computing.smu.edu.sg,...
public class ScoreSheetReader {
    private static final String SEPARATOR = ",";
    private static final String PREFIX = "#";

    // Column positions in the CSV
    private static final int ORG_ID_COLUMN = 0;
    private static final int USERNAME_COLUMN = 1;
    private static final int LAST_NAME_COLUMN = 2;
    private static final int FIRST_NAME_COLUMN = 3;
    private static final int EMAIL_COLUMN = 4;
    private static final int MIN_COLUMNS = EMAIL_COLUMN + 1;

    // Returns the students with their username as the key,
    // e.g. "ping.lee.2023" -> Student. Lines that are too short or have
    // no username are skipped.
    public static HashMap<String, Student> readStudents(String fileName)
            throws FileNotFoundException {
        HashMap<String, Student> students = new HashMap<>();
        Scanner sc = new Scanner(new File(fileName));

        // The first line is the header, not a student
        if (sc.hasNextLine()) {
            sc.nextLine();
        }

        while (sc.hasNextLine()) {
            String[] columns = sc.nextLine().split(SEPARATOR);

            if (columns.length >= MIN_COLUMNS) {
                Student student = createStudent(columns);

                if (!student.getUsername().isEmpty()) {
                    students.put(student.getUsername(), student);
                }
            }
        }

        sc.close();
        return students;
    }

    private static Student createStudent(String[] columns) {
        String orgDefinedId = clean(columns[ORG_ID_COLUMN]);
        String username = clean(columns[USERNAME_COLUMN]);
        String lastName = clean(columns[LAST_NAME_COLUMN]);
        String firstName = clean(columns[FIRST_NAME_COLUMN]);
        String email = clean(columns[EMAIL_COLUMN]);

        return new Student(orgDefinedId, username, lastName, firstName,
                email);
    }

    // Removes spaces and the "#" in front, e.g. "#ping.lee.2023" ->
    // "ping.lee.2023"
    private static String clean(String value) {
        String cleaned = value.trim();

        if (cleaned.startsWith(PREFIX)) {
            cleaned = cleaned.substring(PREFIX.length());
        }

        return cleaned;
    }
}
