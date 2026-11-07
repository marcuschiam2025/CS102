import java.util.ArrayList;
import java.util.Arrays;
import model.Tester;

public class Q2aPart1Tester extends Tester {
    public double grade() {
        Q2aPart1Tester q2aPart1Tester = new Q2aPart1Tester();

        String inputs = "Project sample/TesterFiles/is442.txt";
        String surname = "LEE";
        double expected = -1.0;
        q2aPart1Tester.test(expected, Q2a.getAverageAge(inputs, surname), 1);

        return q2aPart1Tester.getPoints();
    }
    
    public static void main(String[] args) {
        Q2aPart1Tester q2aPart1Tester = new Q2aPart1Tester();
        q2aPart1Tester.grade();
    }
}