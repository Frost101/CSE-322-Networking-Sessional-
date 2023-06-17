package ServerPkg;

public class FileInfo {
    public String fileName;
    public Boolean fileType;        //True:Public....False:Private
    public String path;

    public String filePath;

    public String userName;

    public int fileID;

    FileInfo(){
        fileID = Server.fileID;
        Server.fileID++;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileType(Boolean fileType) {
        this.fileType = fileType;
    }

    public Boolean getFileType() {
        return fileType;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public int getFileID() {
        return fileID;
    }
}
