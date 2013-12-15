package guessinggame;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SimpleServer {
    private static final int PORT = 4444;

    public static void main(String[] args) {
        boolean listening = true;
        ServerSocket serverSocket = null;
        
        try {
            serverSocket = new ServerSocket(PORT);
            while (listening) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ServerHandler(clientSocket)).start();
            }
            serverSocket.close();
        }
        catch (IOException e) {
            System.err.println("Could not listen on port: " + PORT);
            System.exit(1);
        }
    }
    
}
