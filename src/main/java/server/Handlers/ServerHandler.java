package server.Handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import server.AuthService;
import util.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg == null) {
                return;
            }
            if (msg instanceof FileRequest) {
                FileRequest fileRequest = (FileRequest) msg;
                Path pathFile = Paths.get("server_storage/" + fileRequest.getUser() + "/" + fileRequest.getFileName());
                if (Files.exists(pathFile)) {
                    FileMessage fileMessage = new FileMessage(fileRequest.getUser(), pathFile);
                    ctx.writeAndFlush(fileMessage);
                }
            }
            if (msg instanceof FileMessage) {
                FileMessage fm = (FileMessage) msg;
                Files.write(Paths.get("server_storage/" + fm.getUser() + "/" + fm.getFileName()), fm.getData(), StandardOpenOption.CREATE);
                ctx.writeAndFlush(new ListMessage(walkFiles(fm.getUser())));
            }
            if (msg instanceof ListMessage) {
                ListMessage listMessage = (ListMessage) msg;
                listMessage.setList(walkFiles(listMessage.getUser()));
                ctx.writeAndFlush(listMessage);
            }
            if (msg instanceof FileDeleteRequest) {
                FileDeleteRequest deleteRequest = (FileDeleteRequest) msg;
                Files.delete(Paths.get("server_storage/" + deleteRequest.getUser() + "/" + deleteRequest.getFileName()));
                ctx.writeAndFlush(new ListMessage(walkFiles(deleteRequest.getUser())));
            }
            if (msg instanceof AuthRequest) {
                AuthRequest auth = (AuthRequest) msg;
                if (AuthService.auth(auth.getLogin(), auth.getPassword())) {
                    auth.setAuth(true);
                    ctx.writeAndFlush(auth);
                    ctx.writeAndFlush(new ListMessage(walkFiles(auth.getLogin())));
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

    private ArrayList<String> walkFiles(String user) {
        ArrayList<String> list = new ArrayList<>();
        try {
            Files.list(Paths.get("server_storage/" + user + "/")).map(p -> p.getFileName().toString()).forEach(list::add);
            if (list.isEmpty()) {
                list.add("(Папка пустая)");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
}
