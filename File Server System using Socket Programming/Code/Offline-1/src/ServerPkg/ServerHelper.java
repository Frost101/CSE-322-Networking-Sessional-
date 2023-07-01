package ServerPkg;

import java.io.*;
import java.util.Date;
import java.util.Random;

public class ServerHelper {
    public static void sendFile(ObjectInputStream in, ObjectOutputStream out,String username) throws IOException, ClassNotFoundException {
        out.writeObject("Server:Send us the valid File ID");    //Requests client to send valid file id
        int fileID = (Integer) in.readObject();                  //Client sends file id

        /*      First Check if the file ID is valid or not      */
        while(true){
            if(Server.fileInfoHashMap.get(fileID)==null){
                out.writeObject(false);
                fileID = (Integer) in.readObject();                  //Client sends file id again
            }
            else{
                out.writeObject(true);
                break;
            }
        }

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


    public static void receiveFile(ObjectInputStream in, ObjectOutputStream out,String fileName, String username, Boolean fileType,int reqID) throws IOException, ClassNotFoundException {
        out.writeObject("Server:Send The File Size");                      //Request Client to send the file size in bytes
        long fileSize = (long) in.readObject();                            //Receives file size from client

        /*      Now check if file size overflows the MAX_BUFFER_SIZE or not         */
        if(Server.usedBufferSize + fileSize > Server.MAX_BUFFER_SIZE){
            out.writeObject(false);                                                             //Send status to the client.Upload failure
            out.writeObject("Server:ERROR:Buffer Size Overflow...TRANSMISSION TERMINATED!");   //Send Termination message
            System.out.println("ERROR:Buffer Size Overflow...TRANSMISSION TERMINATED!");
            return;
        }
        else{
            Server.usedBufferSize += fileSize;                                          //Allocate buffer
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
        File file = new File(filePath);
        if(file.exists()){

            /*          Delete previous entry of that file      */
            int keyToDelete = -1;
            for(Integer keys : Server.fileInfoHashMap.keySet()){
                if(Server.fileInfoHashMap.get(keys).getFileName().equals(fileName) && Server.fileInfoHashMap.get(keys).getUserName().equals(username)){
                    keyToDelete = keys;
                }
            }
            if(keyToDelete != -1){
                Server.fileInfoHashMap.remove(keyToDelete);
            }
            file.delete();
        }
        FileOutputStream fileOutputStream = new FileOutputStream(filePath);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(filePath));
        int totalBytesRead = 0;

        while ((bytesRead = in.read(buffer)) != -1) {
            totalBytesRead += bytesRead;
            bufferedOutputStream.write(buffer, 0, bytesRead);
            bufferedOutputStream.flush();
            System.out.println("chunk "+chunkNumber+" received (" + bytesRead + " bytes)");
            chunkNumber++;

            /*      Comment out the following lines to generate timeout error       */
//            try{
//                Thread.sleep(40000);
//            }catch (Exception e){
//                e.printStackTrace();
//            }

            out.writeObject("CHUNKS_RECEIVED");                                                 //Send confirmation to client so that he can send the next chunk
        }
        String mssg = (String) in.readObject();                                                 //Completion message from client
        if(mssg.equalsIgnoreCase("THE_END")){
            if(totalBytesRead == fileSize){
                out.writeObject(true);                                    //Send file uploading status
                out.writeObject("File uploaded sucessfully!!!");          //Send success msg to the client
                System.out.println("File Uploaded Successfully!");
                Server.usedBufferSize -= totalBytesRead;                  //Deallocate Buffer

                /*        To add information to the server's file list          */
                FileInfo fileInfo = new FileInfo();
                fileInfo.setUserName(username);
                fileInfo.setFileName(fileName);
                fileInfo.setFilePath(filePath);
                fileInfo.setFileType(fileType);
                Server.fileInfoHashMap.put(fileInfo.getFileID(),fileInfo);      //Update the list
                if(reqID != -1){
                    Request request = Server.reqList.get(reqID);
                    request.setWhoUploaded(username);
                    request.setUploaded(true);
                    String msg = (username + " has uploaded your requested file bearing the requested id = " + request.getReqID() + "  File Name:"+ fileName+"...\nFile is now available at the public file section\nTime: " + new Date());
                    Server.unreadMessagesHashMap.get(request.getWhoRequested()).getUnreadMessages().add(msg);
                }
            }
            else{
                System.out.println("Upload operation failed!");
                out.writeObject(false);                                    //Send file uploading status
                out.writeObject("File upload FAILED.TRY AGAIN");          //Send success msg to the client
                file = new File(filePath);
                file.delete();                                            //Delete the existing chunks
            }
        }
        else{
            /*       Timeout       */
            System.out.println(mssg);
            /*            Now,Delete the chunks that were received              */
            file = new File(filePath);
            file.delete();
            System.out.println("Chunks are deleted for upload failure..Client:"+username);
        }

    }


}
