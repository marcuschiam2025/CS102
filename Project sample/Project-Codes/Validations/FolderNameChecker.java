package Validations;

import java.util.regex.Pattern;

import OwnExceptions.FolderNameException;

public class FolderNameChecker {
    public static void FolderName(String filepath) throws FolderNameException{
        String[] names = filepath.split("/");

        if(Pattern.matches("^[A-Za-z]+[.][0-9]{4}$", names[1]) || Pattern.matches("^[A-Za-z]+[.][A-Za-z]+[.][0-9]{4}$", names[1])){
            System.out.println("Folder name correct");
        }else{
            throw new FolderNameException("Folder name is incorrect");
        }
    }
}
