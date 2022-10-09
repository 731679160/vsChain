package dataOwner;

import server.SP;
import server.ToDOTreeNode;
import tools.SHA;
import tools.StringXor;

import java.util.*;

public class DO {
    List<IdTreeIndex> allTreeIndex = new ArrayList<>();
    HashMap<Integer, UpdState> UMap = new HashMap<>();
    HashMap<String,String> TMap = new HashMap<>();
    HashMap<String,String> DMap = new HashMap<>();
    String k1;
    String k2;

    public HashMap<Integer, UpdState> getUMap() {
        return UMap;
    }

    public HashMap<String, String> getTMap() {
        return TMap;
    }

    public HashMap<String, String> getDMap() {
        return DMap;
    }

    public DO(String k1, String k2){
        this.k1 = k1;
        this.k2 = k2;
    }

    public DO(String k1, String k2, List<InvertedIndex> allInvertedIndex){
        this.k1 = k1;
        this.k2 = k2;
        setup(allInvertedIndex);
    }

    //setup阶段
    public void setup(List<InvertedIndex> allInvertedIndex){
        for (InvertedIndex roundInvertedIndex : allInvertedIndex) {
            IdTreeIndex index = new IdTreeIndex(roundInvertedIndex.getKeyword(), roundInvertedIndex.getIds(), k1);
            allTreeIndex.add(index);
        }
        encryptIndex();
    }

    public void encryptIndex(){
        for(int i = 0;i < allTreeIndex.size();i++){
            IdTreeIndex round = allTreeIndex.get(i);
            String tau_w = round.getTau_w();
            String k_w = SHA.HASHDataToString(k2 + round.getKeyword() + 0);
            int tokenCnt = 0;
            int updCnt = 0;
            String updHash = "";
            UMap.put(round.getKeyword(),new UpdState(tokenCnt,updCnt,updHash,false));
            BinaryTree idTree = round.getTree();
            encryptTreeNode(idTree,tau_w,k_w);
            DMap.put(SHA.HASHDataToString(tau_w), SHA.HASHDataToString(idTree.id + idTree.idHash + idTree.leftHash + idTree.rightHash));
        }
    }

    //加密树中所有节点
    private void encryptTreeNode(BinaryTree root, String tau_w, String k_w){
        if(root == null){
            return;
        }
        encryptTreeNode(root.left,tau_w,k_w);
        String l = SHA.HASHDataToString(tau_w + root.code);
        String v = StringXor.xor(SHA.HASHDataToString(k_w + root.code), String.valueOf(root.id)) + "," + root.idHash + "," + root.leftHash + "," + root.rightHash;
        TMap.put(l,v);
        encryptTreeNode(root.right,tau_w,k_w);
    }


    public HashMap<String, String> update(List<InvertedIndex> indexList) {
        String op = "a";
        HashMap<String, String> updMap = new HashMap<>();
        for (int i = 0; i < indexList.size(); i++) {
            InvertedIndex index = indexList.get(i);
            updMap.putAll(update(index.getKeyword(), index.getIds(), op));
        }
        return updMap;
    }
    public HashMap<String, String> update(int keyword, List<Integer> id, String op) {
        HashMap<String, String> updMap = new HashMap<>();
        for (int i = 0; i < id.size(); i++) {
            updMap.putAll(update(keyword, id.get(i), op));
        }
        return updMap;
    }
    //update阶段
    public HashMap<String, String> update(int keyword,int id,String op){
        String tau_w = SHA.HASHDataToString(k1 + keyword + 0);
        HashMap<String, String> updMap = new HashMap<>();
        UpdState state = UMap.get(keyword);
        if(state == null) {
            state = new UpdState(0, 0, "", false);
            UMap.put(keyword, state);
        }
        if(state.isSearch){
            state.tokenCnt += 1;
            state.updCnt = 1;
            state.updHash = "";
            state.isSearch = false;
        }else{
            state.updCnt += 1;
        }
        String tau_upd_w = SHA.HASHDataToString(k1 + keyword + state.tokenCnt);
        String k_upd_w = SHA.HASHDataToString(k2 + keyword + state.tokenCnt);
        String l = SHA.HASHDataToString(tau_upd_w + state.updCnt);
        String v = StringXor.updXor(SHA.HASHDataToString(k_upd_w + state.updCnt), String.valueOf(id), op) + "," + SHA.HASHDataToString(tau_w + id);
        updMap.put(l,v);
        state.updHash += SHA.HASHDataToString(String.valueOf(id));
        UMap.put(keyword,state);
        return updMap;
    }

    public void updateUpdState(int[] keyword) {
        for (int i = 0; i < keyword.length; i++) {
            UpdState state = UMap.get(keyword[i]);
            state.isSearch = true;
        }
    }

    //search阶段--获取查询令牌
    public List<SearchToken> getSearchToken(int[] keywords){
        List<SearchToken> res = new ArrayList<>(keywords.length);
        for(int i = 0;i < keywords.length;i++){
            int keyword = keywords[i];
            UpdState state = UMap.get(keyword);
            if (state == null) {
                int a =  1;
            }
            String tau_w = SHA.HASHDataToString(k1 + keyword + 0);
            String tau_upd_w = SHA.HASHDataToString(k1 + keyword + state.tokenCnt);
            String k_w = SHA.HASHDataToString(k2 + keyword + 0);
            String k_upd_w = SHA.HASHDataToString(k2 + keyword + state.tokenCnt);
            res.add(new SearchToken(tau_w,tau_upd_w,k_w,k_upd_w));
        }
        return res;
    }

    private boolean preIsLBound = false;
    private boolean isFirstNode = true;
    private boolean LBoundIsLast = false;
    public boolean verification(int[] searchKeywords, List<ToDOTreeNode> rootList, List<String> rootHashList, List<Integer> resList, List<String> tau_wList){
        int n = rootList.size();
        List<List<Integer>> leftBounds = new ArrayList<>(n);
        List<List<Integer>> rightBounds = new ArrayList<>(n);
        //遍历树获取左右边界
        for(int i = 0;i < n;i++){
            UpdState updState = UMap.get(searchKeywords[i]);
            //前遍历所有树，获取所有树中的左右边界，并判断树的更新路径是否正确
            List<Integer> leftBound = new LinkedList<>();
            List<Integer> rightBound = new LinkedList<>();
            ToDOTreeNode root = rootList.get(i);
            preIsLBound = false;
            isFirstNode = true;
            LBoundIsLast = false;
            if(!TravelTree(root, leftBound, rightBound, tau_wList.get(i))) {
                return false;
            }
            if(leftBound.size() != rightBound.size() && LBoundIsLast){
                rightBound.add(Integer.MAX_VALUE);
            }
            //判断根哈希是否正确
            String rootHash = SHA.HASHDataToString(root.id + root.hashId + root.leftHash + root.rightHash);
            if(!Objects.equals(rootHash,rootHashList.get(i))){
                return false;
            }
            leftBounds.add(leftBound);
            rightBounds.add(rightBound);
        }
        //判断边界是否对应
        if(leftBounds.get(0).get(0) != Integer.MIN_VALUE){
            return false;
        }
        int targetId = rightBounds.get(0).get(0);
        int[] sub = new int[n];
        sub[0]++;
        int tarTree = 0;
        while(targetId != Integer.MAX_VALUE){
            int maxRBound = Integer.MIN_VALUE;
            int nextTarTree = tarTree;
            for(int tar = (tarTree + 1) % n;tar != tarTree;tar = (tar + 1) % n){
                int left = leftBounds.get(tar).get(sub[tar]);

                if (sub[tar] >= rightBounds.get(tar).size()) {//
                    return false;
                }

                int right = rightBounds.get(tar).get(sub[tar]);
                sub[tar]++;
                if(targetId >= right || targetId < left){
                    return false;
                }
                if(right >= maxRBound){
                    maxRBound = right;
                    nextTarTree = tar;
                }
                if(targetId != left){//左边界不等于目标id，进入下一轮，并以最大右边界为下一轮目标
                    tarTree = nextTarTree;
                    targetId = maxRBound;
                    break;
                }else if((tar + 1) % n == tarTree){//到达最后一轮，则为查询结果
                    if(resList.get(0) == targetId){
                        tarTree = nextTarTree;
                        targetId = maxRBound;
                        resList.remove(0);
                        break;
                    }else {//和查询结果不一致
                        return false;
                    }
                }
            }
        }
        updateUpdState(searchKeywords);
        return true;
    }

    //前序遍历树，并返回更新节点和所有左右边界，返回null表示验证失败
    private boolean TravelTree(ToDOTreeNode root, List<Integer> leftBound, List<Integer> rightBound, String tau_w){
        boolean leftTravel = true;
        boolean rightTravel = true;
        //左子树
        if(root.left != null){
            //遍历左边
            leftTravel = TravelTree(root.left, leftBound, rightBound, tau_w);
            root.leftHash = SHA.HASHDataToString(root.left.id + root.left.hashId + root.left.leftHash + root.left.rightHash);
        }
        if(!leftTravel) {
            return false;
        }
        //---------------------------------------------------------------------------------------------------
        String[] states = root.state.split(",");
        if(states.length != 0) {
            root.hashId = SHA.HASHDataToString(tau_w + root.id);
        }
        boolean tag = false;
        int id = root.id;//没有id则初始化为-1
        for (String state : states) {
            switch (state) {
                case "l":
                    leftBound.add(id);
                    tag = true;
                    LBoundIsLast = true;
                    break;
                case "r"://右边界的前一个节点必须是左边界
                    if(!preIsLBound && !isFirstNode){
                        return false;
                    }else if(isFirstNode) {
                        leftBound.add(Integer.MIN_VALUE);
                    }
                    rightBound.add(id);
                    break;
            }
        }
        if(!tag){
            LBoundIsLast = false;
        }
        preIsLBound = tag;
        isFirstNode = false;
        //---------------------------------------------------------------------------------------------------
        //右子树
        if(root.right != null){
            //遍历右边
            rightTravel = TravelTree(root.right, leftBound, rightBound, tau_w);
            root.rightHash = SHA.HASHDataToString(root.right.id + root.right.hashId + root.right.leftHash + root.right.rightHash);
        }
        //----------------------------------------------------------------------------------------------------
        return rightTravel;
    }

    public static void main(String[] args) throws Exception {
        String k1 = "1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111";
        String k2 = "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
        String filePath = "./src/invertedIndex.txt";
        List<InvertedIndex> allInvertedIndex = ReadDataToInvertedIndex.readGeneratedData(filePath);
        DO dataOwner = new DO(k1,k2);
        dataOwner.setup(allInvertedIndex);
        SP sp = new SP(dataOwner.TMap);

    }


}
