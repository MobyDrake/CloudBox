package client.Handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.StringProperty;
import util.AuthRequest;
import util.FileMessage;
import util.ListMessage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class ClientHandler extends ChannelInboundHandlerAdapter {

    private ListProperty<String> listServer;
    private ListProperty<String> listLocal;
    private BooleanProperty authentication;
    private StringProperty user;

    public ClientHandler(BooleanProperty authentication, StringProperty user,ListProperty<String> listServer, ListProperty<String> listLocal) {
        this.listServer = listServer;
        this.listLocal = listLocal;
        this.authentication = authentication;
        this.user = user;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg == null) {
            return;
        }
        if (msg instanceof FileMessage) {
            FileMessage fm = (FileMessage) msg;
            Files.write(Paths.get("client_storage/" + fm.getUser() + "/" + fm.getFileName()), fm.getData(), StandardOpenOption.CREATE);
            Platform.runLater(() -> listLocal.add(fm.getFileName()));
            refreshLocalFilesList(fm.getUser());
        }
        if (msg instanceof ListMessage) {
            ListMessage listMessage = (ListMessage) msg;
            Platform.runLater(() -> listServer.setAll(listMessage.getList()));
        }
        if (msg instanceof AuthRequest) {
            AuthRequest auth = (AuthRequest) msg;
            if (auth.isAuth()) {
                Platform.runLater(() -> user.setValue(auth.getLogin()));
                Platform.runLater(() -> authentication.set(true));
                refreshLocalFilesList(auth.getLogin());
            }
        }
    }

    private void refreshLocalFilesList(String user) {
        Platform.runLater(() -> {
            try {
                listLocal.clear();
                if (Files.exists(Paths.get("client_storage/" + user))) {
                    Files.list(Paths.get("client_storage/" + user)).map(p -> p.getFileName().toString()).forEach(o -> listLocal.add(o));
                    if (listLocal.isEmpty()) {
                        listLocal.add("(Папка пустая)");
                    }
                } else {
                    Files.createDirectory(Paths.get("client_storage/" + user + "/"));
                    Files.list(Paths.get("client_storage/" + user)).map(p -> p.getFileName().toString()).forEach(o -> listLocal.add(o));
                    if (listLocal.isEmpty()) {
                        listLocal.add("(Папка пустая)");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
