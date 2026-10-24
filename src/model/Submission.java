package model;

import java.util.ArrayList;

// One student's zip file as it goes through the program:
// unzip -> validate -> fix -> grade
public class Submission {
    private String username; // from the zip file name, e.g. fen.lai.2022
    private Student student; // from the score sheet, null if not in it
    private String zipFilePath;
    private String folderPath; // where it was unzipped
    private ArrayList<Exception> problems; // found by validation
    private ArrayList<String> fixes; // done by the fixer

    public Submission(String username, String zipFilePath) {
        this.username = username;
        this.zipFilePath = zipFilePath;
        this.student = null;
        this.folderPath = null;
        this.problems = new ArrayList<>();
        this.fixes = new ArrayList<>();
    }

    public String getUsername() {
        return username;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public String getZipFilePath() {
        return zipFilePath;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public void setFolderPath(String folderPath) {
        this.folderPath = folderPath;
    }

    public ArrayList<Exception> getProblems() {
        return problems;
    }

    public void addProblem(Exception problem) {
        problems.add(problem);
    }

    public ArrayList<String> getFixes() {
        return fixes;
    }

    public void addFix(String fix) {
        fixes.add(fix);
    }

    @Override
    public String toString() {
        return "Submission [username=" + username
                + ", zipFilePath=" + zipFilePath
                + ", folderPath=" + folderPath
                + ", problems=" + problems.size()
                + ", fixes=" + fixes.size() + "]";
    }
}
