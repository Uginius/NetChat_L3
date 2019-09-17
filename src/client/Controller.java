package client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import javafx.scene.layout.HBox;

public class Controller {
    @FXML
    TextField msgField;
    @FXML
    TextArea chatArea;
    @FXML
    HBox bottomPanel;
    @FXML
    HBox topPanel;
    @FXML
    TextField loginField;
    @FXML
    PasswordField passwordField;

    Socket socket;
    DataInputStream in;
    DataOutputStream out;
    final String ADRESS = "localhost";
    final int PORT = 8181;
    private boolean isAuthorized;

    private void setAuthorized(boolean isAuthorized) {
        this.isAuthorized = isAuthorized;
        if (!isAuthorized) {
            topPanel.setVisible(true);
            topPanel.setManaged(true);
            bottomPanel.setVisible(false);
            bottomPanel.setManaged(false);
        } else {
            topPanel.setVisible(false);
            topPanel.setManaged(false);
            bottomPanel.setVisible(true);
            bottomPanel.setManaged(true);
        }
    }

    public void connect() {
        try {
            socket = new Socket(ADRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> {
                try {
                    auth();
                    read();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    close();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void close() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void read() throws IOException {
        while (true) {
            String str = in.readUTF();
            if (str.equalsIgnoreCase("/serverclosed")) break;
            chatArea.appendText(str + "\n");
        }
    }

    private void auth() throws IOException {
        while (true) {
            String str = in.readUTF();
            if (str.startsWith("/authOK")) {
                setAuthorized(true);
                break;
            } else {
                chatArea.appendText(str + "\n");
            }
        }
    }

    public void tryToAuth(ActionEvent actionEvent) {
        if (socket == null || socket.isClosed()) connect();
        try {
            out.writeUTF("/auth " + loginField.getText() + " " + passwordField.getText());
            loginField.clear();
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(ActionEvent actionEvent) {
        try {
            out.writeUTF(msgField.getText());
            msgField.clear();
            msgField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
