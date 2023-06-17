package ServerPkg;

public class Request {
    public String fileDescription;
    public int reqID;
    public String whoRequested;

    public String whoUploaded;

    public Boolean uploaded;

    public Request(){
        reqID = Server.reqID;
        Server.reqID++;
        uploaded = false;
    }

    public void setUploaded(Boolean uploaded) {
        this.uploaded = uploaded;
    }

    public Boolean getUploaded() {
        return uploaded;
    }

    public void setWhoUploaded(String whoUploaded) {
        this.whoUploaded = whoUploaded;
    }

    public String getWhoUploaded() {
        return whoUploaded;
    }

    public void setFileDescription(String fileDescription) {
        this.fileDescription = fileDescription;
    }

    public String getFileDescription() {
        return fileDescription;
    }

    public void setWhoRequested(String whoRequested) {
        this.whoRequested = whoRequested;
    }

    public String getWhoRequested() {
        return whoRequested;
    }

    public int getReqID() {
        return reqID;
    }
}
