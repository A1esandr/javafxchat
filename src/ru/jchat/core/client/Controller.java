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
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
                            textArea.appendText("Последние записи из локальной истории чата\n");
                            readHistory();
                            textArea.appendText("Конец локальной истории чата\n");
                            break;
                        }
                        textArea.appendText(s + "\n");
                    }
                    while (true) {
                        String s = in.readUTF();
                        textArea.appendText(s + "\n");
                        writeHistory(s);
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
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(historyFileName, true))) {
                writer.write(line + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void readHistory(){
        if(historyFileName != null){
            try {
                tailFile(Paths.get(historyFileName), 100).forEach(line -> textArea.appendText(line + "\n"));
            }
            catch (NoSuchFileException e) {
                System.out.println("Не найден файл истории");
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private List<String> tailFile(final Path source, final int noOfLines) throws IOException {
        try (Stream<String> stream = Files.lines(source)) {
            FileBuffer fileBuffer = new FileBuffer(noOfLines);
            stream.forEach(line -> fileBuffer.collect(line));

            return fileBuffer.getLines();
        }
    }

    private final class FileBuffer {
        private int offset = 0;
        private final int noOfLines;
        private final String[] lines;

        public FileBuffer(int noOfLines) {
            this.noOfLines = noOfLines;
            this.lines = new String[noOfLines];
        }

        public void collect(String line) {
            lines[offset++ % noOfLines] = line;
        }

        public List<String> getLines() {
            return IntStream.range(offset < noOfLines ? 0 : offset - noOfLines, offset)
                    .mapToObj(idx -> lines[idx % noOfLines]).collect(Collectors.toList());
        }
    }
}
