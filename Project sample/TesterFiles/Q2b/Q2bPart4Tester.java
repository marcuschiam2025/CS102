import java.util.ArrayList;
import java.util.Arrays;
import model.Tester;

public class Q2bPart4Tester extends Tester {
    public double grade() {
        Q2bPart4Tester q2bPart4Tester = new Q2bPart4Tester();

        String inputs = "Project sample/TesterFiles/studentstester.txt";
        String courseName = "CS102";
        String expected = "Jason LEE-4.0";
        q2bPart4Tester.test(expected, Q2b.getTopStudent(inputs, courseName), 1);

        return q2bPart4Tester.getPoints();
    }
    
    public static void main(String[] args) {
        Q2bPart4Tester q2bPart4Tester = new Q2bPart4Tester();
        q2bPart4Tester.grade();
        // System.out.println(q1bPart3Tester.grade());
    }
}