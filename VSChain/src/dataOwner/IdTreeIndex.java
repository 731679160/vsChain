package dataOwner;

import tools.SHA;

import java.util.ArrayList;
import java.util.List;

public class IdTreeIndex {
    int keyword;
    BinaryTree tree;
    String tau_w;
    public IdTreeIndex(int keyword, List<Integer> ids, String k1){
        this.keyword = keyword;
        this.tau_w = SHA.HASHDataToString(k1 + keyword + 0);
        this.tree = buildTree(ids);
    }

    private BinaryTree buildTree(List<Integer> ids){
        return buildTreeMain(ids,0,ids.size() - 1,"-1");
    }

    public int getKeyword() {
        return keyword;
    }

    public void setKeyword(int keyword) {
        this.keyword = keyword;
    }

    public BinaryTree getTree() {
        return tree;
    }

    public void setTree(BinaryTree tree) {
        this.tree = tree;
    }


    public String getTau_w() {
        return tau_w;
    }

    public void setTau_w(String tau_w) {
        this.tau_w = tau_w;
    }

    private BinaryTree buildTreeMain(List<Integer> ids, int l, int r, String code){
        if(l > r){
            return null;
        }
        int mid = (l + r) / 2;
        String idHash = SHA.HASHDataToString(tau_w + ids.get(mid));
        BinaryTree left = buildTreeMain(ids,l,mid - 1,code + ",0");
        BinaryTree right = buildTreeMain(ids,mid + 1,r,code + ",1");
        String leftHash;
        String rightHash;
        if(left == null){
            leftHash = "";
        }else{
            leftHash = SHA.HASHDataToString(left.id + left.idHash + left.leftHash + left.rightHash);
        }
        if(right == null){
            rightHash = "";
        }else{
            rightHash = SHA.HASHDataToString(right.id + right.idHash + right.leftHash + right.rightHash);
        }
        return new BinaryTree(ids.get(mid),code,idHash,leftHash,rightHash,left,right);
    }


    public static void main(String[] args) {
        List<Integer> ids = new ArrayList<>();
        ids.add(0);
        ids.add(1);
        ids.add(2);
        ids.add(3);
        ids.add(4);
        ids.add(5);
        ids.add(6);
        IdTreeIndex test = new IdTreeIndex(0,ids,"12331");
        System.out.println(test);
    }
}
