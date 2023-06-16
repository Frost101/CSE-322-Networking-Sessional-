package ServerPkg;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server {

    public static volatile HashMap<String,ClientInfo> clients = new HashMap<>();

    public static volatile int usedBufferSize = 0;
    public static final int MIN_CHUNK_SIZE = 100;
    public static final int MAX_CHUNK_SIZE = 200;
    public static final int MAX_BUFFER_SIZE = 1000000;
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
