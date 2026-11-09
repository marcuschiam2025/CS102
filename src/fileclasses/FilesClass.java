package fileClasses;

public class FilesClass {
    private String fileName;
    private String parentFolder;
    public  FilesClass(String name, String parentName){
        this.fileName = name;
        this.parentFolder = parentName;
    }
    public  String getName(){
        return  this.fileName;
    }
    public  String getParentName(){
        return  this.parentFolder;
    }
}
