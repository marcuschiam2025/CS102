import java.util.ArrayList;
import java.util.Arrays;
import model.Tester;

public class Q2aPart2Tester extends Tester {
    public double grade() {
        Q2aPart2Tester q2aPart2Tester = new Q2aPart2Tester();

        String inputs = "Project sample/TesterFiles/personstester.txt";
        String surname = "TAY";
        double expected = 30.0;
        q2aPart2Tester.test(expected, Q2a.getAverageAge(inputs, surname), 1);

        return q2aPart2Tester.getPoints();
    }
    
    public static void main(String[] args) {
        Q2aPart2Tester q2aPart2Tester = new Q2aPart2Tester();
        q2aPart2Tester.grade();
    }
}