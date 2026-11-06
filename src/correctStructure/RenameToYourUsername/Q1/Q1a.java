<<<<<<< HEAD:src/correctStructure/RenameToYourUsername/Q1/Q1a.java
/*
 * Name:
 * Email ID:
 */

import java.util.*;

public class Q1a {

    /*
     * Write the method getIsogramWords
     */
    public static ArrayList<String> getIsogramWords(ArrayList<String> inputs) {
        // insert your code here.
        
        return new ArrayList<String>(); // to make this code compile. Please modify accordingly!
    }


    // Do not modify the following codes
    public static void main(String[] args) {
        int tcNum = 1;
        {
            ArrayList<String> inputs = new ArrayList<>();
            inputs = new ArrayList<>();
            inputs.add("cat");
            inputs.add("dog");
            inputs.add("tiger");

            System.out.println("-------------------------------------------------------");
            System.out.printf("Test %d: getIsogramWords(%s)%n", tcNum++, inputs);
            String expected = "[cat, dog, tiger]";
            ArrayList<String> actual = getIsogramWords(inputs);
            System.out.println("Expected : " + expected);
            System.out.println("Actual   : " + actual);
            if (expected.equals(actual.toString())) {
                System.out.println("Passed");
            } else {
                System.out.println("Failed");
            }
            System.out.println("-------------------------------------------------------");
        }

        {
            ArrayList<String> inputs = new ArrayList<>();
            inputs = new ArrayList<>();
            inputs.add("alan");
            inputs.add("ben");
            inputs.add("cindy");
            inputs.add("danny");
            inputs.add("evan");
            
            System.out.println("-------------------------------------------------------");
            System.out.printf("Test %d: getIsogramWords(%s)%n", tcNum++, inputs);
            String expected = "[ben, cindy, evan]";
            ArrayList<String> actual = getIsogramWords(inputs);
            System.out.println("Expected : " + expected);
            System.out.println("Actual   : " + actual);
            if (expected.equals(actual.toString())) {
                System.out.println("Passed");
            } else {
                System.out.println("Failed");
            }
            System.out.println("-------------------------------------------------------");
        }

        {
            ArrayList<String> inputs = new ArrayList<>();
            inputs = new ArrayList<>();
            inputs.add("Alan");
            inputs.add("Ben");
            inputs.add("Cindy");
            inputs.add("Danny");
            inputs.add("Evelyne");
            
            System.out.println("-------------------------------------------------------");
            System.out.printf("Test %d: getIsogramWords(%s)%n", tcNum++, inputs);
            String expected = "[Ben, Cindy]";
            ArrayList<String> actual = getIsogramWords(inputs);
            System.out.println("Expected : " + expected);
            System.out.println("Actual   : " + actual);
            if (expected.equals(actual.toString())) {
                System.out.println("Passed");
            } else {
                System.out.println("Failed");
            }
            System.out.println("-------------------------------------------------------");
        }
    }
=======
/*
 * Name:
 * Email ID:
 */

import java.util.*;

public class Q1a {

    /*
     * Write the method getIsogramWords
     */
    public static ArrayList<String> getIsogramWords(ArrayList<String> inputs) {
        // insert your code here.
        ArrayList<String> isograms = new ArrayList<String>();
        
        for (String word : inputs) {
            boolean isIsogram = true;
            ArrayList<Character> characters = new ArrayList<Character>();

            for (int index = 0; index < word.length(); index ++) {
                char character = word.toLowerCase().charAt(index);

                if (characters.contains(character)) {
                    isIsogram = false;
                    break;
                } else {
                    characters.add(character);
                }
            }

            // int i = 1;
            // while (i == 1) {}

            if (isIsogram) {
                isograms.add(word);
            }
        }
        
        return isograms; // to make this code compile. Please modify accordingly!
    }


    // Do not modify the following codes
    public static void main(String[] args) {
        int tcNum = 1;
        {
            ArrayList<String> inputs = new ArrayList<>();
            inputs = new ArrayList<>();
            inputs.add("cat");
            inputs.add("dog");
            inputs.add("tiger");

            System.out.println("-------------------------------------------------------");
            System.out.printf("Test %d: getIsogramWords(%s)%n", tcNum++, inputs);
            String expected = "[cat, dog, tiger]";
            ArrayList<String> actual = getIsogramWords(inputs);
            System.out.println("Expected : " + expected);
            System.out.println("Actual   : " + actual);
            if (expected.equals(actual.toString())) {
                System.out.println("Passed");
            } else {
                System.out.println("Failed");
            }
            System.out.println("-------------------------------------------------------");
        }

        {
            ArrayList<String> inputs = new ArrayList<>();
            inputs = new ArrayList<>();
            inputs.add("alan");
            inputs.add("ben");
            inputs.add("cindy");
            inputs.add("danny");
            inputs.add("evan");
            
            System.out.println("-------------------------------------------------------");
            System.out.printf("Test %d: getIsogramWords(%s)%n", tcNum++, inputs);
            String expected = "[ben, cindy, evan]";
            ArrayList<String> actual = getIsogramWords(inputs);
            System.out.println("Expected : " + expected);
            System.out.println("Actual   : " + actual);
            if (expected.equals(actual.toString())) {
                System.out.println("Passed");
            } else {
                System.out.println("Failed");
            }
            System.out.println("-------------------------------------------------------");
        }

        {
            ArrayList<String> inputs = new ArrayList<>();
            inputs = new ArrayList<>();
            inputs.add("Alan");
            inputs.add("Ben");
            inputs.add("Cindy");
            inputs.add("Danny");
            inputs.add("Evelyne");
            
            System.out.println("-------------------------------------------------------");
            System.out.printf("Test %d: getIsogramWords(%s)%n", tcNum++, inputs);
            String expected = "[Ben, Cindy]";
            ArrayList<String> actual = getIsogramWords(inputs);
            System.out.println("Expected : " + expected);
            System.out.println("Actual   : " + actual);
            if (expected.equals(actual.toString())) {
                System.out.println("Passed");
            } else {
                System.out.println("Failed");
            }
            System.out.println("-------------------------------------------------------");
        }
    }
>>>>>>> faf1c541fe327ae06a7aabca546cc8368218990f:Project sample/Project-Codes/unzippedFolder/2023-2024-fen.lai.2022.zip/RenameToYourStudentID/Q1/Q1a.java
}