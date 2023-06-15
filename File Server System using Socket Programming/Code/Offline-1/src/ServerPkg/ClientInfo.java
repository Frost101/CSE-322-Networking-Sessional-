package ServerPkg;

import java.net.Socket;

public class ClientInfo {
    String username;
    Socket socket;
    Boolean active;

    public ClientInfo(String username,Socket socket){
        this.socket = socket;
        this.username = username;
        active = true;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getActive() {
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
