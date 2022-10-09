package server;

public class ToSCTreeNode {
    public int id;
    public String leftHash;
    public String rightHash;
    public ToSCTreeNode left;
    public ToSCTreeNode right;
    public String state;
    public ToSCTreeNode(int id, String state) {
        this.id = id;
        this.state = state;
    }


}
