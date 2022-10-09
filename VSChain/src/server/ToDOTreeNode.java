package server;

public class ToDOTreeNode {
    public int id = -1;
    public String hashId;
    public String state = "";
    public String leftHash;
    public String rightHash;
    public ToDOTreeNode left;
    public ToDOTreeNode right;

    public void setId(int id) {
        this.id = id;
    }
    public ToDOTreeNode(int id, String hashId) {
        this.id = id;
        this.hashId = hashId;
    }

    public void setState(String state) {
        this.state += (state + ",");
    }
}
