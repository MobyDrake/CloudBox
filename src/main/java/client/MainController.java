package client;

import client.Handlers.ClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import util.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class MainController {

    @FXML
    Button btDownload;
    @FXML
    ListView<String> localFilesList;
    @FXML
    ListView<String> serverFilesList;

    @FXML
    VBox boxAuth;

    @FXML
    VBox boxMain;

    @FXML
    TextField loginField;
    @FXML
    PasswordField passwordField;

    private final String HOST = "localhost";
    private final int PORT = 8182;
    private Channel channel;
    private EventLoopGroup workerGroup;


    //разобраться с пропертями
    private StringProperty login = new SimpleStringProperty();
    private BooleanProperty connected = new SimpleBooleanProperty(false);
    private BooleanProperty authentication = new SimpleBooleanProperty(false);
    private ListProperty<String> refreshServerList = new SimpleListProperty<>();
    private ListProperty<String> refreshLocalList = new SimpleListProperty<>();


    @FXML
    public void initialize() {
        connect();
        boxAuth.visibleProperty().bind(connected);
        boxAuth.managedProperty().bind(connected);
        boxAuth.visibleProperty().bind(authentication.not());
        boxAuth.managedProperty().bind(authentication.not());

        boxMain.visibleProperty().bind(authentication);
        boxMain.managedProperty().bind(authentication);

        refreshServerList.bind(serverFilesList.itemsProperty());
        refreshLocalList.bind(localFilesList.itemsProperty());

    }

    private void connect() {
        workerGroup = new NioEventLoopGroup();

        Task<Channel> task = new Task<Channel>() {

            @Override
            protected Channel call() throws Exception {
                updateMessage("Bootstrapping");
                updateProgress(0.1d, 1.0d);

                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(workerGroup);
                bootstrap.channel(NioSocketChannel.class);
                bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
                //освобождает от указания адреса и порта в bootstrap.connect(HOST, PORT)
                bootstrap.remoteAddress(new InetSocketAddress(HOST, PORT));

                bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(
                                new ObjectDecoder(50 * 1024 * 1024, ClassResolvers.cacheDisabled(null)),
                                new ObjectEncoder(),
                                new ClientHandler(authentication, login, refreshServerList, refreshLocalList));
                    }
                });

                ChannelFuture future = bootstrap.connect();
                Channel chn = future.channel();

                updateMessage("Connecting");
                updateProgress(0.2d, 1.0d);

                future.sync();

                return chn;
            }

            @Override
            protected void succeeded() {
                channel = getValue();
                connected.set(true);
            }

            @Override
            protected void failed() {
                Throwable exc = getException();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка");
                alert.setHeaderText( exc.getClass().getName() );
                alert.setContentText( exc.getMessage() );
                alert.showAndWait();

                connected.set(false);
            }
        };

        new Thread(task).start();
    }

    public void authClient() {
        if (!connected.get()) {
            connect();
        }

        channel.writeAndFlush(new AuthRequest(loginField.getText(), passwordField.getText()));
        loginField.clear();
        passwordField.clear();
    }

    public void refreshLocalFilesList() {
        if (Platform.isFxApplicationThread()) {
            try {
                localFilesList.getItems().clear();
                Files.list(Paths.get("client_storage")).map(p -> p.getFileName().toString()).forEach(o -> localFilesList.getItems().add(o));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Platform.runLater(() -> {
                try {
                    localFilesList.getItems().clear();
                    Files.list(Paths.get("client_storage")).map(p -> p.getFileName().toString()).forEach(o -> localFilesList.getItems().add(o));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void refreshServerFilesList() {
        channel.writeAndFlush(new ListMessage());
    }

    public void downloadFile() {
        channel.writeAndFlush(new FileRequest(serverFilesList.getSelectionModel().getSelectedItem()));
//        channel.writeAndFlush(new FileRequest("1.txt"));
    }

    public void sendFile() {
        try {
            channel.writeAndFlush(new FileMessage(Paths.get("client_storage/" + localFilesList.getSelectionModel().getSelectedItem())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        refreshServerFilesList();
    }

    public void deleteSelectedLocalFile() {
        try {
            Files.delete(Paths.get("client_storage/" + localFilesList.getSelectionModel().getSelectedItem()));
            refreshLocalFilesList();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteSelectedServerFile() {
        channel.writeAndFlush(new FileDeleteRequest(serverFilesList.getSelectionModel().getSelectedItem()));
    }

}
