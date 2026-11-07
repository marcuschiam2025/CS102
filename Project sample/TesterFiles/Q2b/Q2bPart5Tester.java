import java.util.ArrayList;
import java.util.Arrays;
import model.Tester;

public class Q2bPart5Tester extends Tester {
    public double grade() {
        Q2bPart5Tester q2bPart5Tester = new Q2bPart5Tester();

        String inputs = "Project sample/TesterFiles/studentstester.txt";
        String courseName = "CS103";
        String expected = "TAY Evelyn-4.0";
        q2bPart5Tester.test(expected, Q2b.getTopStudent(inputs, courseName), 1);

        return q2bPart5Tester.getPoints();
    }
    
    public static void main(String[] args) {
        Q2bPart5Tester q2bPart5Tester = new Q2bPart5Tester();
        q2bPart5Tester.grade();
        // System.out.println(q1bPart3Tester.grade());
    }
}