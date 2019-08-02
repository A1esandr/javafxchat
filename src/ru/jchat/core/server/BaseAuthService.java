package ru.jchat.core.server;

import java.sql.Connection;
import java.util.List;

public class BaseAuthService implements AuthService {
    private SQLiteJDBC sqLiteJDBC;
    @Override
    public void start() { }
    @Override
    public void stop() { }
    public BaseAuthService(SQLiteJDBC sqLiteJDBC) {
        this.sqLiteJDBC = sqLiteJDBC;
    }
    @Override
    public String getNickByLoginPass(String login, String pass) {
        Connection c = sqLiteJDBC.getConnection();

        List<String> passNick = sqLiteJDBC.getUserPassNick(c, login);

        sqLiteJDBC.closeConnection(c);

        if(passNick == null || passNick.size() != 2){
            return null;
        }

        if(passNick.get(0).equals(pass)){
            return passNick.get(1);
        }

        return null;
    }

    @Override
    public synchronized Integer updateNick(String oldNick, String newNick){
        Connection c = sqLiteJDBC.getConnection();

        List<String> existed = sqLiteJDBC.getLoginByNick(c, newNick);

        if(existed != null && existed.size() > 0){
            return 2;
        }

        Integer updated = sqLiteJDBC.updateNick(c, oldNick, newNick);

        sqLiteJDBC.closeConnection(c);

        return updated;
    }
}

