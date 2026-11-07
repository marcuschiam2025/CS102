import java.util.ArrayList;
import java.util.Arrays;
import model.Tester;

public class Q2aPart5Tester extends Tester {
    public double grade() {
        Q2aPart5Tester q2aPart5Tester = new Q2aPart5Tester();

        String inputs = "Project sample/TesterFiles/personstester.txt";
        String surname = "Ong";
        double expected = 0.0;
        q2aPart5Tester.test(expected, Q2a.getAverageAge(inputs, surname), 1);

        return q2aPart5Tester.getPoints();
    }
    
    public static void main(String[] args) {
        Q2aPart5Tester q2aPart5Tester = new Q2aPart5Tester();
        q2aPart5Tester.grade();
        // System.out.println(q1bPart3Tester.grade());
    }
}