package ClientPkg;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    public static String host = "localhost";
    public static int port = 9999;
    public static void main(String[] args) throws IOException, ClassNotFoundException {

        Scanner sc = new Scanner(System.in);


        Socket socket = new Socket(host,port);
        System.out.println("Connection Established");
        System.out.println("Remote Port: " + socket.getPort());
        System.out.println("Local Port: " + socket.getLocalPort());

        ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

        String welcomeMsg = (String)in.readObject();
        System.out.println(welcomeMsg);
        System.out.println("Username:");
        String username = sc.nextLine();
        out.writeObject(username);
    }
}
