package util;

public class FileRequest extends Message {
    private String fileName;

    public FileRequest(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
