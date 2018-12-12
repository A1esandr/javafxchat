package ru.jchat.core.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Server {
    private ServerSocket server;
    private Vector<ClientHandler> clients;
    private AuthService authService;
    public AuthService getAuthService() {
        return authService;
    }
    private final int PORT = 8189;
    public Server() {
        try {
            server = new ServerSocket(PORT);
            Socket socket = null;
            authService = new BaseAuthService();
            authService.start();
            clients = new Vector<>();
            while (true) {
                System.out.println("Сервер ожидает подключения");
                socket = server.accept();
                System.out.println("Клиент подключился");
                new ClientHandler(this, socket);
                if(socket.isClosed()) break;
            }
        } catch (IOException e) {
            System.out.println("Ошибка при работе сервера");
        } finally {
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            authService.stop();
        }
    }
    public synchronized boolean isNickBusy(String nick) {
        for (ClientHandler o : clients) {
            if (o.getNick().equals(nick)) return true;
        }
        return false;
    }
    public synchronized void broadcastMsg(String msg) {
        for (ClientHandler o : clients) {
            o.sendMsg(msg);
        }
    }
    public synchronized void sendMsgByNick(String nick, String msg) {
        for (ClientHandler o : clients) {
            if(o.getNick().equals(nick)){
                o.sendMsg(msg);
            }
        }
    }
    public synchronized void unsubscribe(ClientHandler o) {
        clients.remove(o);
    }
    public synchronized void subscribe(ClientHandler o) {
        clients.add(o);
    }
}
