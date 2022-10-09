package dataOwner;

public class BinaryTree {
    public int id;
    public String code;
    public String leftHash;
    public String rightHash;
    public BinaryTree left;
    public BinaryTree right;
    public String idHash;

    public BinaryTree(int id, String code, String leftHash, String rightHash){
        this.id = id;
        this.code = code;
        this.leftHash = leftHash;
        this.rightHash = rightHash;
    }

    public BinaryTree(int id, String code, String idHash, String leftHash, String rightHash, BinaryTree left, BinaryTree right){
        this.id = id;
        this.code = code;
        this.leftHash = leftHash;
        this.rightHash = rightHash;
        this.left = left;
        this.right = right;
        this.idHash = idHash;
    }
}
