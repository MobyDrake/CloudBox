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
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import util.FileDeleteRequest;
import util.FileMessage;
import util.FileRequest;
import util.ListMessage;

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

    private final String HOST = "localhost";
    private final int PORT = 8182;
    private Channel channel;
    private EventLoopGroup workerGroup;


    //разобраться с пропертями
    private ListProperty<String> refreshServerList = new SimpleListProperty<>();
    private ListProperty<String> refreshLocalList = new SimpleListProperty<>();



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
                                new ClientHandler(refreshServerList, refreshLocalList));
                    }
                });

                ChannelFuture channelFuture = bootstrap.connect(HOST, PORT).sync();
                Channel chn = channelFuture.channel();
                channel = chn;
                return chn;
            }
        };
        new Thread(task).start();
        refreshServerList.bind(serverFilesList.itemsProperty());
        refreshLocalList.bind(localFilesList.itemsProperty());
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
