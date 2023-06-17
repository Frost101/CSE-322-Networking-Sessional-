package ServerPkg;

import java.awt.*;
import java.io.*;
import java.net.Socket;
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
                if (success1) {
                    System.out.println("Directory created at " + filePath);
                    out.writeObject("Welcome new user ," + username + "...." + "\n A new directory is created under your username in the server");          //Send Welcome Message
                } else {
                    System.out.println("Failed to create directory at " + filePath);
                    out.writeObject("Welcome new user ," + username + "...." + "\n A new directory is created under your username in the server");          //Send Welcome Message
                }
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
                    clientInfo.activeStatus();                                               //Important* = client is now up and running
                }
            }

            String msg = "";
            while (true){
                /*     Catch OPTION_1,OPTION_2....... from client     */
                String options = (String)in.readObject();
                if(options.equalsIgnoreCase("OPTION_1")){
                    /*    Client wants a List of connected clients       */
                    out.writeObject(true);                                              //First send status,True:Logout Success....False:Failure..
                    out.writeObject("Here is the list of clients.....");
                    int count = 1;
                    for(String name : Server.clients.keySet()){
                        msg = count + ". " + name + " ";
                        if(Server.clients.get(name).activeStatus()){
                            msg += "(Online now*)";
                        }
                        else{
                            msg += "(Offline)";
                        }
                        out.writeObject(msg);
                    }
                    out.writeObject("THE_END");                                         //Terminate
                }

                else if(options.equalsIgnoreCase("OPTION_2")){
                    /*          List of that client's uploaded files        */

                    //Send messages to client
                    out.writeObject("Server:Here is the list of your uploaded files");
                    out.writeObject("File ID --- File Name --- File Type ");
                    FileInfo temp;
                    String tmpMsg = "";
                    for(int key : Server.fileInfoHashMap.keySet()){
                        temp = Server.fileInfoHashMap.get(key);
                        tmpMsg = "";
                        if(temp.getUserName().equalsIgnoreCase(username)) {
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
                    if(!status){
                        /*        Client doesn't want to download..so continue...          */
                        continue;
                    }
                    ServerHelper.sendFile(in,out,username);
                }


                else if(options.equalsIgnoreCase("OPTION_3")){
                    /*       Public files of other clients       */

                    //Send messages to client
                    out.writeObject("Server:Here is the list of your uploaded files");
                    out.writeObject("File ID --- File Name --- Uploaded by ");
                    FileInfo temp;
                    String tmpMsg = "";
                    for(int key : Server.fileInfoHashMap.keySet()){
                        temp = Server.fileInfoHashMap.get(key);
                        tmpMsg = "";
                        if(!(temp.getUserName().equalsIgnoreCase(username)) && temp.getFileType()) {
                            tmpMsg += (temp.getFileID() + " --- ");
                            tmpMsg += (temp.getFileName() + " --- ");
                            tmpMsg += (temp.getUserName());
                            out.writeObject(tmpMsg);            //Send file info to client
                        }
                    }
                    out.writeObject("THE_END");             //Send client a signal that the file info sending is complete
                    out.writeObject("Server:Do you want to download?");     //Asks for confirmation if client wants to download any file or not
                    Boolean status = (Boolean) in.readObject();             //True:client wants to download... False:Client doesn't
                    if(!status){
                        /*        Client doesn't want to download..so continue...          */
                        continue;
                    }
                    ServerHelper.sendFile(in,out,username);
                }

                else if(options.equalsIgnoreCase("OPTION_4")){
                    /*              Client makes a file request             */
                    out.writeObject("Server:Send a short description of the file");         //Asks client to give a short description
                    String fileDescription = (String) in.readObject();                      //Client sends short description
                    Request request = new Request();
                    request.setWhoRequested(username);
                    request.setFileDescription(fileDescription);
                    Server.reqList.put(request.getReqID(), request);                        //Add to the reqlist of the server
                    /*              Broadcast this message                  */
                    for(String key : Server.unreadMessagesHashMap.keySet()){
                        if(!key.equalsIgnoreCase(username)){
                            String temp = "Request ID:" + request.getReqID() + "   Description:" + fileDescription;
                            Server.unreadMessagesHashMap.get(key).getUnreadMessages().add(temp);
                        }
                    }
                    out.writeObject("Server:Your request is noted and broadcasted to the other users");     //Send confirmation to the client
                }

                else if(options.equalsIgnoreCase("OPTION_5")){
                    /*          Read Unread Messages            */
                    out.writeObject("Here are the unread messages.........");
                    UnreadMessages temp = Server.unreadMessagesHashMap.get(username);
                    for(String s : temp.getUnreadMessages()){
                        out.writeObject(s);
                        out.writeObject("-------------------------------------------");
                    }
                    temp.getUnreadMessages().clear();
                    out.writeObject("THE_END");                     //Termination
                }

                else if(options.equalsIgnoreCase("OPTION_6")){
                    /*      Client wants to upload a file       */
                    out.writeObject("Server:Send The File Name");                      //Request client to send a file name
                    String fileName = (String) in.readObject();                        //Receives file name from client
                    out.writeObject("Server:Send The File Type(Public or Private");    //Request client to send file type
                    Boolean fileType = (Boolean)in.readObject();                       //Receives fileType from client,True=Public,False=Private
                    out.writeObject("Server:Send The File Size");                      //Request Client to send the file size in bytes
                    long fileSize = (long) in.readObject();                            //Receives file size from client

                    /*      Now check if file size overflows the MAX_BUFFER_SIZE or not         */
                    if(Server.usedBufferSize + fileSize > Server.MAX_BUFFER_SIZE){
                        out.writeObject(false);                                                             //Send status to the client.Upload failure
                        out.writeObject("Server:ERROR:Buffer Size Overflow...TRANSMISSION TERMINATED!");   //Send Termination message
                        System.out.println("ERROR:Buffer Size Overflow...TRANSMISSION TERMINATED!");
                        continue;
                    }
                    else{
                        out.writeObject(true);                                                      //Success true
                        out.writeObject("Server: You may now begin the transmission");              //Permission for uploading granted
                    }


                    Random random = new Random();
                    int chunkSize = random.nextInt( Server.MAX_CHUNK_SIZE - Server.MIN_CHUNK_SIZE + 1) + Server.MIN_CHUNK_SIZE;
                    out.writeObject(chunkSize);                     //Send chunkSize to client
                    int chunkNumber = 1;                            //To track which no of chunk is received
                    byte[] buffer = new byte[chunkSize];
                    int bytesRead;

                    System.out.println(username + " started uploading ...");
                    String filePath = "src/ServerDirectories/" + username + "/" + fileName;                  //Received file path full
                    FileOutputStream fileOutputStream = new FileOutputStream(filePath);
                    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(filePath));
                    int totalBytesRead = 0;

                    while ((bytesRead = in.read(buffer)) != -1) {
                        totalBytesRead += bytesRead;
                        bufferedOutputStream.write(buffer, 0, bytesRead);
                        bufferedOutputStream.flush();
                        System.out.println("chunk "+chunkNumber+" received (" + bytesRead + " bytes)");
                        chunkNumber++;
                        out.writeObject("CHUNKS_RECEIVED");                                                 //Send confirmation to client so that he can send the next chunk
                    }
                    String mssg = (String) in.readObject();                                                 //Completion message from client
                    if(mssg.equalsIgnoreCase("THE_END")){
                        if(totalBytesRead == fileSize){
                            out.writeObject(true);                                    //Send file uploading status
                            out.writeObject("File uploaded sucessfully!!!");          //Send success msg to the client
                            System.out.println("File Uploaded Successfully!");
                            Server.usedBufferSize += totalBytesRead;                  //Update the used Buffer Size

                            /*        To add information to the server's file list          */
                            FileInfo fileInfo = new FileInfo();
                            fileInfo.setUserName(username);
                            fileInfo.setFileName(fileName);
                            fileInfo.setFilePath(filePath);
                            fileInfo.setFileType(fileType);
                            Server.fileInfoHashMap.put(fileInfo.getFileID(),fileInfo);      //Update the list
                        }
                        else{
                            System.out.println("Upload operation failed!");
                            out.writeObject(false);                                    //Send file uploading status
                            out.writeObject("File upload FAILED.TRY AGAIN");          //Send success msg to the client
                            File file = new File(filePath);
                            file.delete();                                            //Delete the existing chunks
                        }
                    }

                }

                else if(options.equalsIgnoreCase("OPTION_7")){
                    /*          Upload requested files              */

                }

                else if(options.equalsIgnoreCase("OPTION_8")){
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
