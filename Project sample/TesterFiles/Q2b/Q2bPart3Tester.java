import java.util.ArrayList;
import java.util.Arrays;
import model.Tester;

public class Q2bPart3Tester extends Tester {
    public double grade() {
        Q2bPart3Tester q2bPart3Tester = new Q2bPart3Tester();

        String inputs = "Project sample/TesterFiles/studentstester.txt";
        String courseName = "CS101";
        String expected = "LEE Teck Yang-4.0";
        q2bPart3Tester.test(expected, Q2b.getTopStudent(inputs, courseName), 1);

        return q2bPart3Tester.getPoints();
    }
    
    public static void main(String[] args) {
        Q2bPart3Tester q2bPart3Tester = new Q2bPart3Tester();
        q2bPart3Tester.grade();
        // System.out.println(q1bPart3Tester.grade());
    }
}