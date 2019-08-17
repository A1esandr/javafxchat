package ru.jchat.core.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nick;
    private static final Logger LOGGER = Logger.getLogger(ClientHandler.class.getName());
    private List<String> stopWords = Arrays.asList("fuck","jerk","asshole");

    public String getNick() {
        return nick;
    }

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> {
                try{
                    while(true){
                        String msg = in.readUTF();
                        if (msg.startsWith("/auth ")){
                            String[] data = msg.split("\\s");
                            String newNick = server.getAuthService().getNickByLoginPass(data[1], data[2]);
                            if (newNick != null){
                                if (!server.isNickBusy(newNick)){
                                    nick = newNick;
                                    sendMsg("/authok");
                                    server.subscribe(this);
                                    break;
                                } else {
                                    sendMsg("Учетная запись уже занята");
                                }
                            } else {
                                sendMsg("Неверный логин/пароль");
                            }
                        }
                    }
                    while(true){
                        String msg = in.readUTF();
                        msg = checkStopWords(msg);
                        LOGGER.info(nick + ": " + msg);
                        if (msg.startsWith("/")){
                            if (msg.equals("/end")){
                                break;
                            } else if(msg.startsWith("/w")){
                                if(msg.contains(" ")){
                                    String[] parts = msg.split(" ");
                                    if(parts.length > 2){
                                        String userNick = parts[1];
                                        String message = String.format("%s to %s: %s", nick, userNick, parts[2]);
                                        server.sendMsgByNick(userNick, message);
                                    }
                                }
                            }
                        } else {
                            server.broadcastMsg(nick + ": " + msg);
                        }
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }finally {
                    nick = null;
                    server.unsubscribe(this);
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg){
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String checkStopWords(String msg){
        if(msg != null){
            for(String word : stopWords){
                if(msg.contains(word)){
                    msg = msg.replaceAll(word, "");
                }
            }
        }
        return msg;
    }
}
