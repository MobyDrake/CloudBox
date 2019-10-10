package server.Handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import util.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

public class MainHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg == null) {
                return;
            }
            if (msg instanceof FileRequest) {
                FileRequest fileRequest = (FileRequest) msg;
                Path pathFile = Paths.get("server_storage/" + fileRequest.getFileName());
                if (Files.exists(pathFile)) {
                    FileMessage fileMessage = new FileMessage(pathFile);
                    ctx.writeAndFlush(fileMessage);
                }
            }
            if (msg instanceof FileMessage) {
                FileMessage fm = (FileMessage) msg;
                Files.write(Paths.get("server_storage/" + fm.getFileName()), fm.getData(), StandardOpenOption.CREATE);
            }
            if (msg instanceof ListMessage) {
                ListMessage listMessage = (ListMessage) msg;
                listMessage.setList(walkFiles());
                ctx.writeAndFlush(listMessage);
            }
            if (msg instanceof FileDeleteRequest) {
                FileDeleteRequest deleteRequest = (FileDeleteRequest) msg;
                Files.delete(Paths.get("server_storage/" + deleteRequest.getFileName()));
                ctx.writeAndFlush(new ListMessage(walkFiles()));
            }
            if (msg instanceof AuthRequest) {
                AuthRequest auth = (AuthRequest) msg;
                if (auth.getLogin().equals("Bob") && auth.getPassword().equals("123")) {
                    auth.setAuth(true);
                    ctx.writeAndFlush(auth);
                    ctx.writeAndFlush(new ListMessage(walkFiles()));
                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    private ArrayList<String> walkFiles() {
        ArrayList<String> list = new ArrayList<>();
        try {
            Files.list(Paths.get("server_storage/")).map(p -> p.getFileName().toString()).forEach(list::add);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
}
