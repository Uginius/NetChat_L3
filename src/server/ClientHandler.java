package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

class ClientHandler {
    private Socket socket;
    private Server server;
    private AuthService authService;
    private DataOutputStream out;
    private DataInputStream in;
    private String nick;
    private List<String> blackList;

    public ClientHandler(Socket socket, Server server) {
        try {
            this.socket = socket;
            this.server = server;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.authService = new AuthServiceImpl();
            this.blackList = new CopyOnWriteArrayList<>();
            new Thread(() -> {
                try {
                    authorization();
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

    private void read() {
        while (true) {
            try {
                String str = in.readUTF();
                // Служебные сообщения
                if (str.startsWith("/")) {
                    if (str.equalsIgnoreCase("/end")) {
                        sendMsg("server closed");
                        break;
                    }
                    if (str.startsWith("/to ")) {
                        String[] tokens = str.split(" ", 3);
                        server.sendPersonalMsg(this, tokens[1], tokens[2]);
                    }
                    if (str.startsWith("/blacklist ")) {
                        String[] tokens = str.split(" ");
                        blackList.add(tokens[1]);
                        sendMsg("You adds " + tokens[1] + " in blackList");
                    }
                    if (str.startsWith("/showblacklist ")) {
                        sendMsg(blackList.toString());
                    }
                    if (str.startsWith("/newnick ")) {
                        String[] tokens = str.split(" ", 3);
                        server.changeMyNick(this, tokens[1]);
                    }
                } else {
                    server.broadcast(this, str);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //   /auth login password
    private void authorization() throws IOException {
        while (true) {
            String str = in.readUTF();
            if (str.startsWith("/auth")) {
                String[] tokens = str.split(" ");
                String authNick = authService.getNick(tokens[1], tokens[2]);
                if (authNick != null) {
                    if (!server.isNickBusy(authNick)) {
                        sendMsg("/authOK");
                        nick = authNick;
                        server.subscribe(this);
                        break;
                    } else sendMsg("This name is already used");
                } else {
                    sendMsg("Incorrect login or password.");
                }
            }
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
        server.unSubscribe(this);
    }

    void sendMsg(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    String getNick() {
        return nick;
    }

    boolean checkBlackList(String nick) {
        return blackList.contains(nick);
    }
}
