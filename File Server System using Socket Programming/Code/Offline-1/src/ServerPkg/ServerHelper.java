package ServerPkg;

import java.io.*;

public class ServerHelper {
    public static void sendFile(ObjectInputStream in, ObjectOutputStream out,String username) throws IOException, ClassNotFoundException {
        out.writeObject("Server:Send us the valid File ID");    //Requests client to send valid file id
        String FID = (String) in.readObject();                  //Client sends file id
        int fileID = Integer.parseInt(FID);
        FileInfo fileInfo = Server.fileInfoHashMap.get(fileID);
        out.writeObject(fileInfo.getFileName());                //Sends file name to the client
        FileInputStream fileInputStream = new FileInputStream(new File(fileInfo.getFilePath()));
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        byte[] chunk = new byte[Server.MAX_CHUNK_SIZE];
        int bytesRead;
        int chunkNumber = 1;
        while ((bytesRead=bufferedInputStream.read(chunk)) != -1){
            System.out.println(bytesRead + " bytes read");
            out.write(chunk, 0, bytesRead);
            out.flush();
            System.out.println("Chunk No " + chunkNumber + " is sent..");
            chunkNumber++;
        }
        out.writeObject("Server:File sent successfully!");
        System.out.println("File sent successfully");
    }
}
