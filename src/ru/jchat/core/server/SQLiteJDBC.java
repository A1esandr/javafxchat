package ru.jchat.core.server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLiteJDBC {
    public Connection getConnection(){
        Connection c = null;

        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:chat.db");
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        System.out.println("Opened database successfully");
        return c;
    }

    public void closeConnection(Connection c){
        System.out.println("Closed connection successfully");
        try {
            c.close();
        } catch ( Exception e ) {
            System.out.println("Error with closing connection");
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
    }

    public List<String> getUserPassNick(Connection c, String login){
        List<String> result = new ArrayList<>();
        try {
            PreparedStatement ps = c.prepareStatement("SELECT pass, nick FROM user where login = ?");
            ps.setString(1, login);

            ResultSet rs = ps.executeQuery();

            while ( rs.next() ) {
                String pass = rs.getString("pass");
                result.add(pass);
                String nick = rs.getString("nick");
                result.add(nick);
            }
            rs.close();
            ps.close();
        } catch ( Exception e ) {
            System.out.println("Error with db query");
            System.out.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        System.out.println("Operation done successfully");
        return result;
    }

    public List<String> getLoginByNick(Connection c, String nick){
        List<String> result = new ArrayList<>();
        try {
            PreparedStatement ps = c.prepareStatement("SELECT login FROM user where nick = ?");
            ps.setString(1, nick);

            ResultSet rs = ps.executeQuery();

            while ( rs.next() ) {
                String login = rs.getString("login");
                result.add(login);
            }
            rs.close();
            ps.close();
        } catch ( Exception e ) {
            System.out.println("Error with db query");
            System.out.println( e.getClass().getName() + ": " + e.getMessage() );
        }
        System.out.println("Operation done successfully");
        return result;
    }

    public Integer updateNick(Connection c, String oldNick, String newNick){
        Integer result = 1;
        try {
            PreparedStatement ps = c.prepareStatement("UPDATE user SET nick = ? where nick = ?");
            ps.setString(1, newNick);
            ps.setString(2, oldNick);

            ps.executeUpdate();
            ps.close();
        } catch ( Exception e ) {
            System.out.println("Error with db query");
            System.out.println( e.getClass().getName() + ": " + e.getMessage() );
            result = 0;
        }
        System.out.println("Operation done successfully");
        return result;
    }
}
