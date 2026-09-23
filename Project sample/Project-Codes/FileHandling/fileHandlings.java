package fileHandlings;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class fileHandlings {
    public static ArrayList<String> readFiles(String delimeterPattern, String fileName) {
        File f = new File(fileName);
        ArrayList<String> values = new ArrayList<>();
        try (Scanner sc = new Scanner(f);) {
            sc.useDelimiter(delimeterPattern);
            while (sc.hasNext()) {
                String s = sc.next();
                values.add(s);
            }
            sc.close();
        } catch (FileNotFoundException e) {
            System.out.println("The file is not found");
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
