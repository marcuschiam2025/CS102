package fileclasses;
import java.util.ArrayList;

public class Folder extends FilesClass{
    private ArrayList<FilesClass> filesInsideFolder;
    public  Folder(String nameOfFolder, String parentName){
        super(nameOfFolder, parentName);
    }
    public  void  setFilesInsideFolder(ArrayList<FilesClass> filesParam){
        this.filesInsideFolder = filesParam;
    }
    public  ArrayList<FilesClass> retrieveFilesInsideFolder(){
        return  this.filesInsideFolder;
    }
}
