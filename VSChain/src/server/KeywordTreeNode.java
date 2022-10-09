package server;

public class KeywordTreeNode {
    public int id;
    public String idHash;
    public String leftHash;
    public String rightHash;
    public KeywordTreeNode left;
    public KeywordTreeNode right;
    public KeywordTreeNode(){

    }

    public KeywordTreeNode(int id, String idHash, String leftHash, String rightHash) {
        this.id = id;
        this.idHash = idHash;
        this.leftHash = leftHash;
        this.rightHash = rightHash;
    }
}
