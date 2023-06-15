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
            ClientInfo clientInfo = null;
            if(Server.clients.get(username) == null){
                clientInfo = new ClientInfo(username,socket);
                clientInfo.activeStatus();
                Server.clients.put(username,clientInfo);
                out.writeObject(true);                                              //Send connection status.True:success..False:Failure
                out.writeObject("Welcome new user ," + username + "....");          //Send Welcome Message
            }
            else{
                clientInfo = Server.clients.get(username);
                if(clientInfo.activeStatus()){
                    /*     Already another client with the same username is connected.So do immediate termination with a proper error message      */
                    out.writeObject(false);                                                                         //Send connection status.True:success..False:Failure
                    out.writeObject("Already a client is logged in with the same username.CONNECTION TERMINATED!");     //Send Failure Message
                    socket.close();
                    return;
                }
                else{
                    out.writeObject(true);                                                   //Send connection status.True:success..False:Failure
                    out.writeObject("Welcome back ," + username + "....");                   //Send Welcome Message
                    clientInfo.setSocket(socket);                                            //Setup new socket
                    clientInfo.activeStatus();                                              //Important* = client is now up and running
                }
            }

            while (true){
                /*     Catch OPTION_1,OPTION_2....... from client     */
                String options = (String)in.readObject();
                if(options.equalsIgnoreCase("OPTION_1")){

                }
                else if(options.equalsIgnoreCase("OPTION_7")){
                    /*    Client wants to Logout     */
                    out.writeObject(true);                                              //First send status,True:Logout Successful....False:Logout Failure
                    out.writeObject("Logout Successful!!!CONNECTION TERMINATED...");    //Send Message
                    clientInfo.getSocket().close();                                     //Close Connection
                    break;
                }
                else{

                }
            }


        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
