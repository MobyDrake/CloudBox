package util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileMessage extends Message {
    private String fileName;
    private String user;
    private byte[] data;

    public FileMessage(String user, Path path) throws IOException {
        this.fileName = path.getFileName().toString();
        this.data = Files.readAllBytes(path);
        this.user = user;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getData() {
        return data;
    }

    public String getUser() {
        return user;
    }
}
