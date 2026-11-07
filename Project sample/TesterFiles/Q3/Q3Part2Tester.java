import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import model.Tester;

public class Q3Part2Tester extends Tester {
    public double grade() {
        Q3Part2Tester q3Part2Tester = new Q3Part2Tester();

        ArrayList<Shape> inputs = new ArrayList<>();
        inputs.add(new Rectangle("R1", 100, 2));
        inputs.add(new Rectangle("R2", 200, 1));
        inputs.add(new Rectangle("R3", 4, 50));
        inputs.add(new Rectangle("R4", 40, 5));
        String expected = "[R2=>200.0:402.0, R1=>200.0:204.0, R3=>200.0:108.0, R4=>200.0:90.0]";
        q3Part2Tester.test(expected, Q3.sortShapes(inputs), 1);

        return q3Part2Tester.getPoints();
    }
    
    public static void main(String[] args) {
        Q3Part2Tester q3Part2Tester = new Q3Part2Tester();
        q3Part2Tester.grade();
    }
}