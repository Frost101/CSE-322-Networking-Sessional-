package ServerPkg;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ServerThread extends Thread{
    Socket socket;

    public ServerThread(Socket socket){
        this.socket = socket;
    }

    public void run(){
        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            out.writeObject("Welcome to the server.Please send us your Username");
            String username = (String) in.readObject();
            if(Server.clients.get(username) == null){
                ClientInfo clientInfo = new ClientInfo(username,socket);
                clientInfo.setActive(true);
                Server.clients.put(username,clientInfo);
            }


        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
