import java.io.*;

public class TestPaper {
    public static double testPaper() {
        // File[] files = new File("Tester-Files").listFiles();

        // for (int index = 0; index < files.length; index ++) {
        //     System.out.println(files[index]);
        // }

        Tester[] testers = {new Q1aTester(), new Q1bTester()};
        double points = 0;

        for (Tester tester : testers) {
            points += tester.grade();
        }

        return points;
    }

    public static void main(String[] args) {
        System.out.println(TestPaper.testPaper());
    }
}
