package client.Handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import util.FileMessage;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class ClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg == null) {
            return;
        }
        if (msg instanceof FileMessage) {
            FileMessage fm = (FileMessage) msg;
            Files.write(Paths.get("client_storage/" + fm.getFileName()), fm.getData(), StandardOpenOption.CREATE);
        }
    }
}
