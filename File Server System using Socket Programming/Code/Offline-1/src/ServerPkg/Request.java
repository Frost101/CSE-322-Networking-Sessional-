package ServerPkg;

public class Request {
    public String fileDescription;
    public int reqID;
    public String whoRequested;

    public Request(){
        reqID = Server.reqID;
        Server.reqID++;
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
