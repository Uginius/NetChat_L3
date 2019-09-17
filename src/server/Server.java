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

    void broadcast(String message) {
        for (ClientHandler clientHandler : peers) {
            clientHandler.sendMsg(message);

        }

    }

    void subscribe(ClientHandler clientHandler) {
        peers.add(clientHandler);
    }

    void unSubscribe(ClientHandler clientHandler) {
        peers.remove(clientHandler);
    }
}
