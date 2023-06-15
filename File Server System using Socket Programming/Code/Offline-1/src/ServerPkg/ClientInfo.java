package ServerPkg;

import java.io.Serializable;
import java.net.Socket;

public class ClientInfo implements Serializable {
    String username;
    Socket socket;
    Boolean active;

    public ClientInfo(String username,Socket socket){
        this.socket = socket;
        this.username = username;
        active = !socket.isClosed();
    }

    public boolean activeStatus(){
        active = !socket.isClosed();
        return active;
    }

    public void setName(String name) {
        this.username = name;
    }

    public String getName() {
        return username;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }
}
