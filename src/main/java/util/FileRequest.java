package util;

public class FileRequest extends Message {
    private String fileName;
    private String user;

    public FileRequest(String user, String fileName) {
        this.fileName = fileName;
        this.user = user;
    }

    public String getFileName() {
        return fileName;
    }

    public String getUser() {
        return user;
    }
}
