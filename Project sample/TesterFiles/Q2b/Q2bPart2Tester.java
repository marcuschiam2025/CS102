import java.util.ArrayList;
import java.util.Arrays;
import model.Tester;

public class Q2bPart2Tester extends Tester {
    public double grade() {
        Q2bPart2Tester q2bPart2Tester = new Q2bPart2Tester();

        try {
            String inputs = "Project sample/TesterFiles/studentstester.txt";
            String courseName = "SMU101";
            q2bPart2Tester.printResult("(DataException)", Q2b.getTopStudent(inputs, courseName), false, 0);
        } catch (DataException ex){
            q2bPart2Tester.printResult("(DataException)", "(DataException)", true, 1);
        } catch (Exception e) {
            q2bPart2Tester.printResult("(DataException)", "(" + e.getClass().getSimpleName() + ")", true, 0);
        }

        return q2bPart2Tester.getPoints();
    }
    
    public static void main(String[] args) {
        Q2bPart2Tester q2bPart2Tester = new Q2bPart2Tester();
        q2bPart2Tester.grade();
    }
}