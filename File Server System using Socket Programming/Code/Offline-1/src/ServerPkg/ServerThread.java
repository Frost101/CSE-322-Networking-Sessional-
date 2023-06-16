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
                boolean success1 = directory.mkdir();
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

                else if(options.equalsIgnoreCase("OPTION_6")){
                    /*      Client wants to upload a file       */
                    out.writeObject("Send The File Name");          //Request client to send a file name
                    String fileName = (String) in.readObject();     //Receives file name from client
                    out.writeObject("Send The File Size");          //Request Client to send the file size in bytes
                    long fileSize = (long) in.readObject();         //Receives file size from client
                    Random random = new Random();
                    int chunkSize = random.nextInt( Server.MAX_CHUNK_SIZE - Server.MIN_CHUNK_SIZE + 1) + Server.MIN_CHUNK_SIZE;
                    out.writeObject(chunkSize);                     //Send chunkSize to client
                    int chunkNumber = 1;
                    byte[] buffer = new byte[chunkSize];
                    int bytesRead;

                    System.out.println("Uploading starts");
                    String fName = "chunk_" + chunkNumber + ".dat";
                    String filePath = "src/ServerDirectories/" + username + "/" + fileName;
                    FileOutputStream fileOutputStream = new FileOutputStream(filePath);
                    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(filePath));

                    while ((bytesRead = in.read(buffer)) != -1) {
                        bufferedOutputStream.write(buffer, 0, bytesRead);
                        bufferedOutputStream.flush();
                        System.out.println("chunk "+chunkNumber+" received " + bytesRead + " bytes received");
                        chunkNumber++;
                        out.writeObject("CHUNKS_RECEIVED");
                    }
                    String mssg = (String) in.readObject();
                    System.out.println(mssg);
                    System.out.println("File uploaded successfully!!!!");

//                    /*       Merge The Chunks       */
//                    String outputFile = "src/ServerDirectories/" + username + "/" + fileName;
//                    String inputDirectory = "src/ServerDirectories/" + username;
//                    FileOutputStream fos = new FileOutputStream(outputFile);
//                    File directory = new File(inputDirectory);
//                    File[] chunkFiles = directory.listFiles((dir, name) -> name.endsWith(".dat"));
//
//
//                    for (File chunkFile : chunkFiles) {
//                        FileInputStream fis = new FileInputStream(chunkFile);
//                        buffer = new byte[chunkSize];
//                        while ((bytesRead = fis.read(buffer)) != -1) {
//                            fos.write(buffer, 0, bytesRead);
//                        }
//                    }
//                    System.out.println("Merged Successfully!!!");
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
