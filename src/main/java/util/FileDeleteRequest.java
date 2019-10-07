package util;

public class FileDeleteRequest extends Message {
    private String fileName;

    public FileDeleteRequest(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
