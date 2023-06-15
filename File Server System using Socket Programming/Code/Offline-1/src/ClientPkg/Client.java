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
            System.out.println("1.List of clients connected in the server(Type 1)");
            System.out.println("2.List of your uploaded files(Type 2)");
            System.out.println("3.Public files of other clients(Type 3)");
            System.out.println("4.Make a file request(Type 4)");
            System.out.println("5.Read Unread Messages(Type 5)");
            System.out.println("6.Upload a file(Type 6)");
            System.out.println("7.Logout(Type 7");
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

            }
            else if(options == 2){

            }
            else if(options == 3){

            }
            else if(options == 4){

            }
            else if(options == 5){

            }
            else if(options == 6){

            }
            else if(options == 7){
                /*   Log out  */
                out.writeObject("OPTION_7");

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
