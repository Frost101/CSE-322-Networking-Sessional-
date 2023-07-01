package ClientPkg;

import java.io.*;
import java.net.SocketTimeoutException;

public class ClientHelper {
    public static void receiveFile(ObjectInputStream in, ObjectOutputStream out, String username) throws IOException, ClassNotFoundException {
        String fileName = (String) in.readObject();             //Server sends filename
        int chunkSize = 1024;
        int chunkNumber = 1;                            //To track which no of chunk is received
        byte[] buffer = new byte[chunkSize];
        int bytesRead;

        System.out.println(" Download Starts ....");
        String filePath = "src/ClientDownloads/" + username;                  //Received file path full
        File directory = new File(filePath);
        directory.mkdirs();                                                   //Create a directory for storing downloaded files
        filePath += ("/"+fileName);
        FileOutputStream fileOutputStream = new FileOutputStream(filePath);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(filePath));
        int totalBytesRead = 0;

        while ((bytesRead = in.read(buffer)) != -1) {
            totalBytesRead += bytesRead;
            bufferedOutputStream.write(buffer, 0, bytesRead);
            bufferedOutputStream.flush();
            System.out.println("chunk "+chunkNumber+" received (" + bytesRead + " bytes)");
            chunkNumber++;
        }
        String mssg = (String) in.readObject();                 //Termination msg sent from server
        System.out.println(mssg);
        System.out.println("File downloaded successfully!!Total "+totalBytesRead+" bytes received");
    }


    public static void sendFile(ObjectOutputStream out, ObjectInputStream in,File file,String filePath) throws IOException, ClassNotFoundException {


        Boolean isUploadDone = true;               //To trace the timeout scenario
        String serverMsg;
        serverMsg = (String) in.readObject();      //Server asks for the file size
        long fileSize = file.length();
        System.out.println("File size "+fileSize+" bytes.Sending this information to the server");
        out.writeObject(fileSize);                 //Send File size to the server

        /*     Wait for server's confirmation about buffer size     */
        Boolean stat = (Boolean) in.readObject();
        serverMsg = (String) in.readObject();
        if(stat){
            /*      Begin the Transmission      */
            System.out.println(serverMsg);
        }
        else{
            /*          Overflow        */
            System.out.println(serverMsg);
            return;
        }

        int chunkSize = (int)in.readObject();      //Receives chunksize from server
        System.out.println("Chunk Size = " + chunkSize + "  is received from server");
        FileInputStream fileInputStream = new FileInputStream(new File(filePath));
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        byte[] chunk = new byte[chunkSize];
        int bytesRead;
        int chunkNumber = 1;
        while ((bytesRead=bufferedInputStream.read(chunk)) != -1){
            System.out.println(bytesRead + " bytes read");
            out.write(chunk, 0, bytesRead);
            out.flush();

            try{
                while (true){
                    String confMsg = (String) in.readObject();
                    if(confMsg.equalsIgnoreCase("CHUNKS_RECEIVED")){
                        break;
                    }
                }
                System.out.println("Chunk No " + chunkNumber + " is sent..");
                chunkNumber++;
            }catch (SocketTimeoutException e){
                System.out.println("TIMEOUT!Server did not acknowldge chunk within 30 seconds..UPLOAD FAILED");
                isUploadDone = false;           //Upload is not successfull
                break;
            }

        }

        if(isUploadDone){
            out.writeObject("THE_END");
            Boolean status = (Boolean) in.readObject();             //Receives Server status
            serverMsg = (String) in.readObject();
            if(status){
                /*    File upload is successfull      */
                System.out.println(serverMsg);
            }
            else{
                /*      Unsuccessful        */
                System.out.println(serverMsg);
            }
        }
        else{
            /*        Upload is not successfull.Send Server the timeout message        */
            serverMsg = (String) in.readObject();           //Dump chunk received msg
            out.writeObject("TIMEOUT");

        }


    }
}
