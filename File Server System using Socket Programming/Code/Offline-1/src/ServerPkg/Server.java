package ServerPkg;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {

    public static volatile HashMap<String,ClientInfo> clients = new HashMap<>();

    public static volatile int usedBufferSize = 0;
    public static final int MIN_CHUNK_SIZE = 100;
    public static final int MAX_CHUNK_SIZE = 200;
    public static final int MAX_BUFFER_SIZE = 80000;
    public static volatile int fileID = 1;
    public static volatile HashMap<Integer,Request> reqList = new HashMap<>();

    public static volatile HashMap<String,UnreadMessages> unreadMessagesHashMap = new HashMap<>();

    public static volatile int reqID = 1;

    public static volatile ArrayList<FileInfo> fileList = new ArrayList<>();    //To store uploaded file info

    public static volatile HashMap<Integer,FileInfo> fileInfoHashMap = new HashMap<Integer, FileInfo>();
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
