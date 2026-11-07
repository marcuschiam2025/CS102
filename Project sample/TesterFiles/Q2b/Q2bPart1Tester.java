import java.util.ArrayList;
import java.util.Arrays;
import model.Tester;

public class Q2bPart1Tester extends Tester {
    public double grade() {
        Q2bPart1Tester q2bPart1Tester = new Q2bPart1Tester();

        try {
            String inputs = "Project sample/TesterFiles/is442.txt";
            String courseName = "IS101";
            q2bPart1Tester.printResult("(DataException)", Q2b.getTopStudent(inputs, courseName), false, 0);
        } catch (DataException ex){
            q2bPart1Tester.printResult("(DataException)", "(DataException)", true, 1);
        } catch (Exception e) {
            q2bPart1Tester.printResult("(DataException)", "(" + e.getClass().getSimpleName() + ")", true, 0);
        }

        return q2bPart1Tester.getPoints();
    }
    
    public static void main(String[] args) {
        Q2bPart1Tester q2bPart1Tester = new Q2bPart1Tester();
        q2bPart1Tester.grade();
    }
}