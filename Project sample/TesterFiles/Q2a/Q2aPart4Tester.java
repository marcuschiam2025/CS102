import java.util.ArrayList;
import java.util.Arrays;
import model.Tester;

public class Q2aPart4Tester extends Tester {
    public double grade() {
        Q2aPart4Tester q2aPart4Tester = new Q2aPart4Tester();

        String inputs = "Project sample/TesterFiles/personstester.txt";
        String surname = "Wong";
        double expected = 26.0;
        q2aPart4Tester.test(expected, Q2a.getAverageAge(inputs, surname), 1);

        return q2aPart4Tester.getPoints();
    }
    
    public static void main(String[] args) {
        Q2aPart4Tester q2aPart4Tester = new Q2aPart4Tester();
        q2aPart4Tester.grade();
        // System.out.println(q1bPart3Tester.grade());
    }
}