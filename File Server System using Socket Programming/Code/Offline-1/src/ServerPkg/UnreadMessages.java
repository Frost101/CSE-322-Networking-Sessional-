package ServerPkg;

import java.util.ArrayList;

public class UnreadMessages {
    String username;
    ArrayList<String> unreadMessages;

    UnreadMessages(String username){
        unreadMessages = new ArrayList<>();
        this.username = username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUnreadMessages(ArrayList<String> unreadMessages) {
        this.unreadMessages = unreadMessages;
    }

    public ArrayList<String> getUnreadMessages() {
        return unreadMessages;
    }
}
