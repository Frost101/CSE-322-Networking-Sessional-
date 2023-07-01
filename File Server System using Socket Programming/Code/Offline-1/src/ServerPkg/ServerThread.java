package ServerPkg;

import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;
import java.util.Random;

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

                /*      First Login.So create a directory       */
                String filePath = "src/ServerDirectories/" + username;
                File directory = new File(filePath);
                Server.unreadMessagesHashMap.put(username,new UnreadMessages(username));
                boolean success1 = directory.mkdirs();
                System.out.println("Directory created at " + filePath);
                out.writeObject("Server:Welcome new user ," + username + "...." + "\n A new directory is created under your username in the server");
            }
            else{
                clientInfo = Server.clients.get(username);
                if(clientInfo.activeStatus()){
                    /*     Already another client with the same username is connected.So do immediate termination with a proper error message      */
                    out.writeObject(false);                                                                         //Send connection status.True:success..False:Failure
                    out.writeObject("Server:Already a client is logged in with the same username.CONNECTION TERMINATED!");     //Send Failure Message
                    socket.close();
                    return;
                }
                else{
                    out.writeObject(true);                                                   //Send connection status.True:success..False:Failure
                    out.writeObject("Welcome back ," + username + "....");                   //Send Welcome Message
                    clientInfo.setSocket(socket);                                            //Setup new socket
                    clientInfo.activeStatus();                                               //Important* = client is now up and running
                }
            }

            String msg = "";
            try {
                while (true) {
                    /*     Catch OPTION_1,OPTION_2....... from client     */
                    String options = (String) in.readObject();
                    if (options.equalsIgnoreCase("OPTION_1")) {
                        /*    Client wants a List of connected clients       */
                        out.writeObject(true);                                              //First send status,True:Logout Success....False:Failure..
                        out.writeObject("Here is the list of clients.....");
                        int count = 1;
                        for (String name : Server.clients.keySet()) {
                            msg = count + ". " + name + " ";
                            if (Server.clients.get(name).activeStatus()) {
                                msg += "(Online now*)";
                            } else {
                                msg += "(Offline)";
                            }
                            out.writeObject(msg);
                        }
                        out.writeObject("THE_END");                                         //Terminate
                    } else if (options.equalsIgnoreCase("OPTION_2")) {
                        /*          List of that client's uploaded files        */

                        //Send messages to client
                        out.writeObject("Server:Here is the list of your uploaded files");
                        out.writeObject("File ID --- File Name --- File Type ");
                        FileInfo temp;
                        String tmpMsg = "";
                        for (int key : Server.fileInfoHashMap.keySet()) {
                            temp = Server.fileInfoHashMap.get(key);
                            tmpMsg = "";
                            if (temp.getUserName().equalsIgnoreCase(username)) {
                                tmpMsg += (temp.getFileID() + " --- ");
                                tmpMsg += (temp.getFileName() + " --- ");
                                if (temp.getFileType()) tmpMsg += "Public";
                                else tmpMsg += "Private";
                                out.writeObject(tmpMsg);            //Send file info to client
                            }
                        }
                        out.writeObject("THE_END");             //Send client a signal that the file info sending is complete
                        out.writeObject("Server:Do you want to download?");     //Asks for confirmation if client wants to download any file or not
                        Boolean status = (Boolean) in.readObject();             //True:client wants to download... False:Client doesn't
                        if (!status) {
                            /*        Client doesn't want to download..so continue...          */
                            continue;
                        }
                        ServerHelper.sendFile(in, out, username);
                    } else if (options.equalsIgnoreCase("OPTION_3")) {
                        /*       Public files of other clients       */

                        //Send messages to client
                        out.writeObject("Server:Here is the list of your uploaded files");
                        out.writeObject("File ID --- File Name --- Uploaded by ");
                        FileInfo temp;
                        String tmpMsg = "";
                        for (int key : Server.fileInfoHashMap.keySet()) {
                            temp = Server.fileInfoHashMap.get(key);
                            tmpMsg = "";
                            if (!(temp.getUserName().equalsIgnoreCase(username)) && temp.getFileType()) {
                                tmpMsg += (temp.getFileID() + " --- ");
                                tmpMsg += (temp.getFileName() + " --- ");
                                tmpMsg += (temp.getUserName());
                                out.writeObject(tmpMsg);            //Send file info to client
                            }
                        }
                        out.writeObject("THE_END");             //Send client a signal that the file info sending is complete
                        out.writeObject("Server:Do you want to download?");     //Asks for confirmation if client wants to download any file or not
                        Boolean status = (Boolean) in.readObject();             //True:client wants to download... False:Client doesn't
                        if (!status) {
                            /*        Client doesn't want to download..so continue...          */
                            continue;
                        }
                        ServerHelper.sendFile(in, out, username);
                    } else if (options.equalsIgnoreCase("OPTION_4")) {
                        /*              Client makes a file request             */
                        out.writeObject("Server:Send a short description of the file");         //Asks client to give a short description
                        String fileDescription = (String) in.readObject();                      //Client sends short description
                        Request request = new Request();
                        request.setWhoRequested(username);
                        request.setFileDescription(fileDescription);
                        Server.reqList.put(request.getReqID(), request);                        //Add to the reqlist of the server
                        /*              Broadcast this message                  */
                        for (String key : Server.unreadMessagesHashMap.keySet()) {
                            if (!key.equalsIgnoreCase(username)) {
                                String temp = "Request ID:" + request.getReqID() + "   Description:" + fileDescription + "\nRequested By:" + username + "\nTime:" + new Date();
                                Server.unreadMessagesHashMap.get(key).getUnreadMessages().add(temp);
                            }
                        }
                        out.writeObject("Server:Your request is noted and broadcasted to the other users");     //Send confirmation to the client
                    } else if (options.equalsIgnoreCase("OPTION_5")) {
                        /*          Read Unread Messages            */
                        out.writeObject("Here are the unread messages.........");
                        UnreadMessages temp = Server.unreadMessagesHashMap.get(username);
                        for (String s : temp.getUnreadMessages()) {
                            out.writeObject(s);
                            out.writeObject("-------------------------------------------");
                        }
                        temp.getUnreadMessages().clear();
                        out.writeObject("THE_END");                     //Termination
                    } else if (options.equalsIgnoreCase("OPTION_6")) {
                        /*      Client wants to upload a file       */
                        out.writeObject("Server:Send The File Name");                      //Request client to send a file name
                        String fileName = (String) in.readObject();                        //Receives file name from client
                        out.writeObject("Server:Send The File Type(Public or Private");    //Request client to send file type
                        Boolean fileType = (Boolean) in.readObject();                       //Receives fileType from client,True=Public,False=Private
                        ServerHelper.receiveFile(in, out, fileName, username, fileType, -1);
                    } else if (options.equalsIgnoreCase("OPTION_7")) {
                        /*          Upload requested files              */
                        out.writeObject("Server:Send a valid request ID");
                        int reqID = (Integer) in.readObject();               //Client sends reqID

                        while(true){
                            /*      Check if it's a valid request ID or not     */
                            if(Server.reqList.get(reqID) == null){
                                out.writeObject(false);                         //Invalid req id..
                                reqID = (Integer) in.readObject();               //Client sends reqID again
                                continue;
                            }
                            else {
                                out.writeObject(true);
                                break;
                            }
                        }
                        out.writeObject("Server:Send The File Name");                      //Request client to send a file name
                        String fileName = (String) in.readObject();                        //Receives file name from client
                        ServerHelper.receiveFile(in, out, fileName, username, true, reqID);

                    } else if (options.equalsIgnoreCase("OPTION_8")) {
                        /*    Client wants to Logout     */
                        out.writeObject(true);                                              //First send status,True:Logout Successful....False:Logout Failure
                        out.writeObject("Logout Successful!!!CONNECTION TERMINATED...");    //Send Message
                        clientInfo.getSocket().close();                                     //Close Connection
                        break;
                    } else {
                        /*      DUMP        */
                    }
                }
            }catch (IOException | ClassNotFoundException e ){
                /*      If Client Gets Disconnected.Log him out     */
                clientInfo.getSocket().close();
            }


        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
