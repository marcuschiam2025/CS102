import java.util.ArrayList;
import java.util.Arrays;
import model.Tester;

public class Q2aPart3Tester extends Tester {
    public double grade() {
        Q2aPart3Tester q2aPart3Tester = new Q2aPart3Tester();

        String inputs = "Project sample/TesterFiles/personstester.txt";
        String surname = "LEE";
        double expected = 37.0;
        q2aPart3Tester.test(expected, Q2a.getAverageAge(inputs, surname), 1);

        return q2aPart3Tester.getPoints();
    }
    
    public static void main(String[] args) {
        Q2aPart3Tester q2aPart3Tester = new Q2aPart3Tester();
        q2aPart3Tester.grade();
        // System.out.println(q1bPart3Tester.grade());
    }
}