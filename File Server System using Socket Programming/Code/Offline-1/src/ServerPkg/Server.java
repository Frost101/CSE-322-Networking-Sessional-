package ServerPkg;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server {

    public static volatile HashMap<String,ClientInfo> clients = new HashMap<>();
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(9999);
        while(true){
            System.out.println("Waiting For Connection...");
            Socket socket = serverSocket.accept();
            System.out.println("Connection Established!");
            ServerThread serverThread = new ServerThread(socket);
            serverThread.start();

        }

    }
}
