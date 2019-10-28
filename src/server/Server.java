package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

class StartServer {
    public static void main(String[] args) {
        new Server();
    }
}

public class Server {
    private List<ClientHandler> peers;

    public Server() {
        AuthService authService = new AuthServiceImpl();
        peers = new CopyOnWriteArrayList<>();
        ServerSocket serverSocket = null;
        Socket socket = null;

        try {
            authService.connect();
            serverSocket = new ServerSocket(8181);
            System.out.println("Server started.");
            while (true) {
                socket = serverSocket.accept();
                System.out.println("Client connected.");
                new ClientHandler(socket, this);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            authService.disconnect();
        }
    }

    void broadcast(ClientHandler from, String message) {
        for (ClientHandler clientHandler : peers) {
            if (!clientHandler.checkBlackList(from.getNick())) clientHandler.sendMsg(from.getNick() + ": " + message);

        }
    }

    void subscribe(ClientHandler clientHandler) {
        peers.add(clientHandler);
    }

    void unSubscribe(ClientHandler clientHandler) {
        peers.remove(clientHandler);
    }

    void sendPersonalMsg(ClientHandler from, String nickTo, String msg) {
        for (ClientHandler o : peers) {
            if (o.getNick().equalsIgnoreCase(nickTo) && !o.checkBlackList(from.getNick())) {
                o.sendMsg("FROM: " + from.getNick() + " SEND: " + msg);
                from.sendMsg("TO: " + nickTo + " SEND: " + msg);
                return;
            }
        }
        from.sendMsg("Nick " + nickTo + " is not found");
    }

    public boolean isNickBusy(String nick) {
        for (ClientHandler o : peers) if (o.getNick().equalsIgnoreCase(nick)) return true;
        return false;
    }

    public void changeMyNick(ClientHandler from, String newname) {
    }
}
