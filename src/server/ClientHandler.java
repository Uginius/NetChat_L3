package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

class ClientHandler {
    private Socket socket;
    private Server server;
    private AuthService authService;
    private DataOutputStream out;
    private DataInputStream in;

    public ClientHandler(Socket socket, Server server) {
        try {
            this.socket = socket;
            this.server = server;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            this.authService = new AuthServiceImpl();
            new Thread(() -> {
                try {
                    autorization();
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
                if (str.equalsIgnoreCase("/end")) {
                    sendMsg("serverclosed");
                    break;
                }
                server.broadcast(str);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //   /auth login password
    private void autorization() throws IOException {
        while (true) {
            String str = in.readUTF();
            if (str.startsWith("/auth")) {
                String[] tokens = str.split(" ");
                String nick = authService.getNick(tokens[1], tokens[2]);
                if (nick != null) {
                    sendMsg("/authOK");
                    server.subscribe(this);
                    break;
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

}
