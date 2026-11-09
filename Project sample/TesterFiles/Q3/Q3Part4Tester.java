import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import model.Tester;

public class Q3Part4Tester extends Tester {
    public double grade() {
        Q3Part4Tester q3Part4Tester = new Q3Part4Tester();

        ArrayList<Shape> inputs = new ArrayList<>();
        inputs.add(new Circle("C1", 5));
        inputs.add(new Circle("C2", 3));
        inputs.add(new Rectangle("R2", 8, 3));
        inputs.add(new Rectangle("R1", 12, 2));
        inputs.add(new Rectangle("R3", 4, 6));
        String expected = "[C1=>79.0:31.0, C2=>28.0:19.0, R1=>24.0:28.0, R2=>24.0:22.0, R3=>24.0:20.0]";
        q3Part4Tester.test(expected, String.valueOf(Q3.sortShapes(inputs)), 1);

        return q3Part4Tester.getPoints();
    }
    
    public static void main(String[] args) {
        Q3Part4Tester q3Part4Tester = new Q3Part4Tester();
        q3Part4Tester.grade();
        // System.out.println(q1bPart3Tester.grade());
    }
}