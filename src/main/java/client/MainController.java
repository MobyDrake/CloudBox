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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import util.FileRequest;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    Button btDownload;
    @FXML
    ListView<String> localFilesList;
    @FXML
    ListView<String> serverFilesList;

    final String HOST = "localhost";
    final int PORT = 8182;
    private Channel channel;
    private EventLoopGroup workerGroup;


    //разобраться с пропертями
//    private StringProperty refreshList = new SimpleStringProperty("");
//    private BooleanProperty downloadFile = new SimpleBooleanProperty(false);



    @Override
    public void initialize(URL location, ResourceBundle resources) {

        workerGroup = new NioEventLoopGroup();

        Task<Channel> task = new Task<Channel>() {

            @Override
            protected Channel call() throws Exception {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(workerGroup);
                bootstrap.channel(NioSocketChannel.class);
                bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
                bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(
                                new ObjectDecoder(50 * 1024 * 1024, ClassResolvers.cacheDisabled(null)),
                                new ObjectEncoder(),
                                new ClientHandler());
                    }
                });

                ChannelFuture channelFuture = bootstrap.connect(HOST, PORT).sync();
                Channel chn = channelFuture.channel();
                channel = chn;
                return chn;
            }
        };
        new Thread(task).start();
        refreshLocalFilesList();
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

    public void downloadFile() {
//        channel.writeAndFlush(new FileRequest(serverFilesList.getSelectionModel().getSelectedItem()));
        channel.writeAndFlush(new FileRequest("1.txt"));
        refreshLocalFilesList();
    }

    public void deleteSelectedLocalFile() {
        try {
            Files.delete(Paths.get("client_storage/" + localFilesList.getSelectionModel().getSelectedItem()));
            refreshLocalFilesList();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
