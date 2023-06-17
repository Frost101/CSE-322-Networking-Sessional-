package ClientPkg;

import java.io.*;
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

        String loginMsg = (String)in.readObject();          //To get login and send username message from server
        System.out.println(loginMsg);
        System.out.print("Username:");
        String username = sc.nextLine();
        out.writeObject(username);                          //Send your username for validation
        Boolean success = (Boolean)in.readObject();         //Read connection status.True:Success..False:Failure
        String welcomeMsg = (String)in.readObject();        //Get welcome or error message
        System.out.println(welcomeMsg);

        if(!success){
            /*   If already another person with the same username is logged in then terminate the connection   */
            return;
        }

        while(true){
            /*        Here options are displayed-what a client can do         */
            System.out.println("------------------------------");
            System.out.println("Here are the things you can do/explore...");
            System.out.println("1.List of clients connected(at least once) to the server(Type 1)");
            System.out.println("2.List of your uploaded files(Type 2)");
            System.out.println("3.Public files of other clients(Type 3)");
            System.out.println("4.Make a file request(Type 4)");
            System.out.println("5.Read Unread Messages(Type 5)");
            System.out.println("6.Upload a file(Type 6)");
            System.out.println("7.Upload requested files(Type 7)");
            System.out.println("8.Logout(Type 8)");
            System.out.println("-------------------------------");

            String serverMsg;           //To store the messages sent from server
            Boolean serverStatus;       //To receive status - success/failure
            int options;
            try{
                options = Integer.parseInt(sc.nextLine());
            }catch (Exception e){
                System.out.println("Invalid Input!Try again....");              // If a non integer input is given mistakenly
                continue;
            }

            if(options == 1){
                /*     List of clients connected to the server     */
                out.writeObject("OPTION_1");
                serverStatus = (Boolean) in.readObject();       //Receive the server status
                if(serverStatus){
                    serverMsg = (String) in.readObject();       //Read Server Messages
                    while (!serverMsg.equalsIgnoreCase("THE_END")){
                        System.out.println(serverMsg);
                        serverMsg = (String) in.readObject();
                    }
                }
                else{
                    System.out.println("Something went wrong!Please try again...");
                }


            }
            else if(options == 2){
                /*         List of my uploaded files        */
                out.writeObject("OPTION_2");
                serverMsg = (String) in.readObject();
                while(!serverMsg.equalsIgnoreCase("THE_END")){
                    System.out.println(serverMsg);
                    serverMsg = (String) in.readObject();
                }
                serverMsg = (String) in.readObject();           //Server is asking if I want to downloa any file or not
                Boolean status;
                while (true){
                    System.out.println(serverMsg);
                    System.out.println("YES(Type 1)");
                    System.out.println("NO(type 2)");
                    String msg = sc.nextLine();
                    if(msg.equalsIgnoreCase("1")){
                        status = true;
                        break;
                    }
                    else if(msg.equalsIgnoreCase("2")){
                        status = false;
                        break;
                    }
                    else{
                        System.out.println("Invalid Option!try again");
                    }
                }
                out.writeObject(status);                        //Status:True(download),false(no)
                if(!status)continue;                            //If client doesn't want to download then continue
                serverMsg = (String) in.readObject();           //Server sends the msg "Send the valid file ID"
                System.out.println(serverMsg);
                System.out.print("Enter:");
                String FID = sc.nextLine();
                out.writeObject(FID);                           //Send file ID to the server
                ClientHelper.receiveFile(in,out,username);
            }

            else if(options == 3){
                /*       Public files of  other clients       */
                out.writeObject("OPTION_3");
                serverMsg = (String) in.readObject();
                while(!serverMsg.equalsIgnoreCase("THE_END")){
                    System.out.println(serverMsg);
                    serverMsg = (String) in.readObject();
                }
                serverMsg = (String) in.readObject();           //Server is asking if I want to downloa any file or not
                Boolean status;
                while (true){
                    System.out.println(serverMsg);
                    System.out.println("YES(Type 1)");
                    System.out.println("NO(type 2)");
                    String msg = sc.nextLine();
                    if(msg.equalsIgnoreCase("1")){
                        status = true;
                        break;
                    }
                    else if(msg.equalsIgnoreCase("2")){
                        status = false;
                        break;
                    }
                    else{
                        System.out.println("Invalid Option!try again");
                    }
                }
                out.writeObject(status);                        //Status:True(download),false(no)
                if(!status)continue;                            //If client doesn't want to download then continue
                serverMsg = (String) in.readObject();           //Server sends the msg "Send the valid file ID"
                System.out.println(serverMsg);
                System.out.print("Enter:");
                String FID = sc.nextLine();
                out.writeObject(FID);                           //Send file ID to the server
                ClientHelper.receiveFile(in,out,username);
            }
            else if(options == 4){
                    /*          Make a file Request         */
                out.writeObject("OPTION_4");
                serverMsg = (String) in.readObject();           //Server asks for a file description
                System.out.println(serverMsg);
                System.out.print("Description:");
                String fileDescription = sc.nextLine();
                out.writeObject(fileDescription);               //Send file description to the server
                serverMsg = (String) in.readObject();            //Server sends confirmation
                System.out.println(serverMsg);
            }
            else if(options == 5){
                /*          See Unread Messages         */
                out.writeObject("OPTION_5");
                serverMsg = (String) in.readObject();
                while (!serverMsg.equalsIgnoreCase("THE_END")){
                    System.out.println(serverMsg);
                    serverMsg = (String) in.readObject();
                }
            }
            else if(options == 6){
                /*       Upload a file        */
                out.writeObject("OPTION_6");
                serverMsg = (String) in.readObject();       //Server asks to send a file name
                System.out.println(serverMsg);
                String fileName = sc.nextLine();

                String filePath = "src/ClientDirectories/" + fileName;
                File file = new File(filePath);

                while(true){
                    if(file.exists()){
                        break;
                    }
                    else{
                        System.out.println("File not found.Enter a valid file name...");
                        fileName = sc.nextLine();
                        filePath = "src/ClientDirectories/" + fileName;
                        file = new File(filePath);
                    }
                }
                out.writeObject(fileName);                 //Send File Name to the server


                serverMsg = (String) in.readObject();      //Server asks for file type
                boolean fileType;                          //True:Public,False:Private
                while (true){
                    System.out.println(serverMsg);
                    System.out.println("Public(Type 1)");
                    System.out.println("Private(Type 2)");
                    String tmp = sc.nextLine();
                    if(tmp.equalsIgnoreCase("1")){
                        fileType = true;
                        break;
                    }
                    else if(tmp.equalsIgnoreCase("2")){
                        fileType = false;
                        break;
                    }
                    else{
                        System.out.println("Invalid Input!Try again...");
                    }
                }
                out.writeObject(fileType);                  //Send server the filetype,public or private
                ClientHelper.sendFile(out,in,file,filePath);
            }

            else if(options == 7){
                /*      Upload Requested Files      */
                out.writeObject("OPTION_7");
                serverMsg = (String) in.readObject();               //Server asks for a valid request id
                int reqID;

                while (true){
                    try {
                        System.out.println(serverMsg);
                        System.out.print("Request ID:");
                        String tmp = sc.nextLine();
                        reqID = Integer.parseInt(tmp);
                        break;
                    }catch (Exception e){
                        System.out.println("Invalid input!Try Again...");
                    }
                }
                out.writeObject(reqID);                              //Sends server the request ID
                serverMsg = (String) in.readObject();       //Server asks to send a file name
                System.out.println(serverMsg);
                String fileName = sc.nextLine();

                String filePath = "src/ClientDirectories/" + fileName;
                File file = new File(filePath);

                while(true){
                    if(file.exists()){
                        break;
                    }
                    else{
                        System.out.println("File not found.Enter a valid file name...");
                        fileName = sc.nextLine();
                        filePath = "src/ClientDirectories/" + fileName;
                        file = new File(filePath);
                    }
                }
                out.writeObject(fileName);                 //Send File Name to the server
                ClientHelper.sendFile(out,in,file,filePath);
            }

            else if(options == 8){
                /*   Log out  */
                out.writeObject("OPTION_8");

                serverStatus = (Boolean) in.readObject();       //Receive the server status
                if(serverStatus){
                    serverMsg = (String) in.readObject();       //Read Server Messages
                    System.out.println(serverMsg);
                    socket.close();
                    break;
                }
                else{
                    System.out.println("Something went wrong during LOGOUT...Try again!");
                }
            }
        }
    }
}
