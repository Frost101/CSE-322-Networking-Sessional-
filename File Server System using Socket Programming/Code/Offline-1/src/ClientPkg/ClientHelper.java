package ClientPkg;

import java.io.*;

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
}
