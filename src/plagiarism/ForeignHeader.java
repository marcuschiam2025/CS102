package plagiarism;

public class ForeignHeader {

    private String ownerUsername;
    private String foundUsername;
    private String fileName;
    private String line;

    public ForeignHeader(String ownerUsername, String foundUsername,
            String fileName, String line) {
        this.ownerUsername = ownerUsername;
        this.foundUsername = foundUsername;
        this.fileName = fileName;
        this.line = line;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public String getFoundUsername() {
        return foundUsername;
    }

    public String getFileName() {
        return fileName;
    }

    public String getLine() {
        return line;
    }
}
