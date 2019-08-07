package ru.jchat.core.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable{
    @FXML
    TextArea textArea;
    @FXML
    TextField msgField;
    @FXML
    HBox authPanel;
    @FXML
    HBox msgPanel;
    @FXML
    TextField loginField;
    @FXML
    PasswordField passField;

    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private String historyFileName;
    private List<String> messages;

    final String SERVER_IP = "localhost";
    final int SERVER_PORT = 8189;

    private boolean authorized;

    public void setAuthorized(boolean authorized) {
        this.authorized = authorized;
        if (authorized){
            msgPanel.setVisible(true);
            msgPanel.setManaged(true);
            authPanel.setVisible(false);
            authPanel.setManaged(false);
        } else {
            msgPanel.setVisible(false);
            msgPanel.setManaged(false);
            authPanel.setVisible(true);
            authPanel.setManaged(true);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setAuthorized(false);
    }

    public void connect(){
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            messages = new ArrayList<>();
            new Thread(this::resetSocketOnTimeout).start();
            Thread t = new Thread(() -> {
                try {
                    while(true){
                        String s = in.readUTF();
                        if (s.startsWith("/authok")){
                            setAuthorized(true);
                            if(s.contains(" ")){
                                String[] parts = s.split(" ");
                                if(parts.length > 1){
                                    historyFileName = "history_" + parts[1] + ".txt";
                                }
                            }
                            break;
                        }
                        textArea.appendText(s + "\n");
                    }
                    while (true) {
                        String s = in.readUTF();
                        if (s.startsWith("/history")){
                            textArea.appendText(s.substring(8) + "\n");
                        } else {
                            textArea.appendText(s + "\n");
                            writeHistory(s);
                        }
                    }
                } catch (IOException e) {
                    showAlert("Сервер перестал отвечать");
                } finally {
                    setAuthorized(false);
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            t.setDaemon(true);
            t.start();
        } catch (IOException e) {
            showAlert("Не удалось подключиться к серверу. Проверьте сетевое соединение");
        }
    }

    public void sendAuthMsg(){
        if (socket == null || socket.isClosed()){
            connect();
        }
        try { // "/auth login pass"
            out.writeUTF("/auth " + loginField.getText() + " " + passField.getText());
            loginField.clear();
            passField.clear();
        } catch (IOException e) {
            showAlert("Не удалось подключиться к серверу. Проверьте сетевое соединение");
        }
    }

    public void sendMsg(){
        try {
            out.writeUTF(msgField.getText());
            msgField.clear();
            msgField.requestFocus();
        } catch (IOException e) {
            showAlert("Не удалось подключиться к серверу. Проверьте сетевое соединение");
        }
    }

    public void showAlert(String msg){
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Возникли проблемы");
            alert.setHeaderText(null);
            alert.setContentText(msg);
            alert.showAndWait();
        });
    }

    private void resetSocketOnTimeout() {
        try {
            Thread.sleep(120000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (!authorized) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeHistory(String line){
        if(historyFileName != null && line != null){
            int historySize = messages.size();
            if(historySize == 0 || (historySize % 10) > 0){
                messages.add(line);
            } else {
                messages.add(line);
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(historyFileName, true))) {
                    for(int i = historySize - 10; i < historySize; i++){
                        writer.write(messages.get(i) + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
