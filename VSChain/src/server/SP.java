package server;

import dataOwner.SearchToken;
import tools.SHA;
import tools.StringXor;

import java.util.*;

public class SP {
    HashMap<String,String> TMap;
    HashMap<String, KeywordTreeNode> treesMap;

    public SP(HashMap<String,String> TMap){
        this.TMap = TMap;
        treesMap = new HashMap<>();
    }

    public void update(HashMap<String, String> updMap) {
        TMap.putAll(updMap);
    }
    public SearchOutput search(List<SearchToken> tokens){
        int n = tokens.size();
        List<Integer> result = new LinkedList<>();
        List<ToDOTreeNode> VOToDO = new ArrayList<>(n);
        List<ToSCTreeNode> VOToSC = new ArrayList<>(n);
        List<List<UpdData>> keyword_upd_ids = new ArrayList<>(n);

        //取到更新的关键字列表-----------------------------------------------------------------------------------
        for (SearchToken token : tokens) {
            List<UpdData> upd_ids = new ArrayList<>();
            String tau_upd_w = token.getTau_upd_w();
            String k_upd_w = token.getK_upd_w();
            for (int c = 1; ; c++) {
                String l_upd = SHA.HASHDataToString(tau_upd_w + c);
                String v_upd = TMap.get(l_upd);
                if (v_upd == null) {
                    break;
                }
                UpdData idAndIdHashAndOp = StringXor.updDeXor(SHA.HASHDataToString(k_upd_w + c), v_upd);
                bipInsert(upd_ids, idAndIdHashAndOp);
                //获取更新id以后就将其从TMap中删除
                TMap.remove(l_upd);
            }
            keyword_upd_ids.add(upd_ids);
        }
        //-------------------------------------------------------------------------------------------------------

        //扫描关键字树------------------------------------------------------------------------------------------------------------
        //服务器在扫描各关键字树时，为每棵树维护一个栈，在第一次扫描到一个节点时入栈，添加id值，在扫描节点左子树后再返回到该节点时，添加左孩子
        //哈希值，在扫描节点右子树后，将该节点出栈，并添加其右孩子哈希值。
        //注意在扫描需要更新节点时，DoNode需要有具体id值，ScNode需要有id的哈希值，对于不需要更新的节点，DoNode需要id的哈希值，ScNode为null
        //注意在扫描到左右边界时，DoNode需要具体id值，ScNode为null
        //为每个关键字树初始化一个栈，用来存储扫描到的节点
        List<Stack<StackNode>> stacks = new ArrayList<>(n);
        //tagList用来保存根节点到扫描节点路径中比扫描节点id值大的节点，从而快速找到比目标节点大的路径
        List<List<Integer>> tagList = new ArrayList<>(n);
        //先将各关键字树的根节点入栈
        for(int i = 0;i < n;i++){
            String tau_w = tokens.get(i).getTau_w();
            String k_w = tokens.get(i).getK_w();
            Stack<StackNode> stack = new Stack<>();
            KeywordTreeNode node;
            List<UpdData> updIds = new ArrayList<>(keyword_upd_ids.get(i));
            boolean updTag = updIds.size() != 0;//判断是否有更新
            ToDOTreeNode DoNode = null;
            ToSCTreeNode ScNode = null;
            if(treesMap.containsKey(tau_w)){//服务器已保存该树
                node = treesMap.get(tau_w);
            }else{
                String l = SHA.HASHDataToString(tau_w + "-1");
                String v = TMap.get(l);
                TMap.remove(l);
                if(v == null) {//若该关键字没有根节点，再从更新列表里找，若都没有则返回查询结果空
                    if(updIds.size() == 0) {
                        System.out.println("没有该关键字");
                        return null;
                    }else {
                        int t = updIds.size() / 2;
                        UpdData updData = updIds.get(t);
                        int id = updData.getId();
                        String idHash = updData.getIdHash();
                        ScNode = new ToSCTreeNode(id, "new");
                        updIds.remove(t);
                        node = new KeywordTreeNode(id, idHash, "", "");
                    }
                } else {
//                    String[] data = v.split(",");
                    node = StringXor.deXor(SHA.HASHDataToString(k_w + "-1"), v);
//                    idHash = data[1];
//                    if(data.length == 1){
//                        node = new KeywordTreeNode(id, idHash, "", "");
//                    }else {
//                        node = new KeywordTreeNode(id, idHash, data[2], data[3]);
//                    }
                }
                treesMap.put(tau_w,node);
            }
            DoNode = new ToDOTreeNode(node.id, node.idHash);
            //没有需要更新的文档
            if(!updTag){
                stack.add(new StackNode(node, DoNode,"-1"));
            }else{//有需要更新的文档
                if(ScNode == null) {
                    ScNode = new ToSCTreeNode(node.id, "old");
                }
                int j = 0;
                for(;j < updIds.size();j++){
                    if(updIds.get(j).getId() > node.id){
                        break;
                    }
                }
                stack.add(new StackNode(node, DoNode, ScNode, updIds, j,"-1"));
                VOToSC.add(ScNode);
            }
            VOToDO.add(DoNode);
            stacks.add(stack);
            tagList.add(new LinkedList<>());
        }

        //找到第一颗树的最左端节点---------------------------------------------------------------
        int targetTree = 0;
        String tau_w = tokens.get(0).getTau_w();
        String k_w = tokens.get(0).getK_w();
        Stack<StackNode> stack = stacks.get(0);
        StackNode preNode = stack.peek();
        String tarCode = preNode.code + ",0";
        int targetId;
        String idHash;
        List<Integer> tagIds = tagList.get(0);
        KeywordTreeNode node = preNode.node.left;
        String v = null;
        if(node == null){
            String l = SHA.HASHDataToString(tau_w + tarCode);
            v = TMap.get(l);
            TMap.remove(l);
        }
        while(node != null || v != null) {
            tagIds.add(preNode.node.id);//因为一直向左走，所以要一直将preNode节点id保存
            if (node == null){
//                String[] data = v.split(",");
                node = StringXor.deXor(SHA.HASHDataToString(k_w + tarCode), v);
                targetId = node.id;
                idHash = node.idHash;
//                if(data.length == 1){
//                    node = new KeywordTreeNode(targetId, idHash, "","");
//                }else{
//                    node = new KeywordTreeNode(targetId, idHash, data[1], data[2]);
//                }
            }else {
                targetId = node.id;
                idHash = node.idHash;
            }
            ToDOTreeNode DoNode = new ToDOTreeNode(targetId, idHash);
            if(preNode.upd_ids == null || preNode.dot == 0){//左边没有需要更新的文档
                stack.add(new StackNode(node,DoNode,tarCode));
            }else{//左边有需要更新的文档
                ToSCTreeNode ScNode = new ToSCTreeNode(targetId, "old");
                List<UpdData> stackIds = copySubList(preNode.upd_ids,0,preNode.dot);
                int i = 0;
                for(;i < stackIds.size();i++){
                    if(stackIds.get(i).getId() >= targetId){
                        break;
                    }
                }
                stack.add(new StackNode(node, DoNode, ScNode, stackIds, i, tarCode));
            }
            tarCode += ",0";
            node = node.left;
            if(node == null){
                String l = SHA.HASHDataToString(tau_w + tarCode);
                v = TMap.get(l);
                TMap.remove(l);
            }
            preNode = stack.peek();
        }
        List<UpdData> stackIds = preNode.upd_ids;
        //说明树的最左端还有需要更新的节点
        while(stackIds!= null && preNode.dot != 0){
            tagIds.add(preNode.node.id);//因为一直向左走，所以要一直将preNode节点id保存
            stackIds = copySubList(stackIds, 0, preNode.dot);
            int mid = (stackIds.size() - 1) / 2;
            targetId = stackIds.get(mid).getId();
            idHash = stackIds.get(mid).getIdHash();
            stackIds.remove(mid);
            node = new KeywordTreeNode(targetId, idHash, "","");
            ToDOTreeNode DoNode = new ToDOTreeNode(targetId, idHash);
            ToSCTreeNode ScNode = new ToSCTreeNode(targetId, "new");
            stack.add(new StackNode(node,DoNode, ScNode, stackIds,mid,tarCode));
            preNode = stack.peek();
            tarCode += ",0";
        }
        //初始化为第一个右边界{
        preNode = stack.peek();
        targetId = preNode.node.id;
        preNode.DONode.setState("r");

        preNode.DONode.setId(targetId);

        //??????
        tagList.set(0,tagIds);
        //--------------------------------------------------------------------------------------


        //轮流扫描各关键字树，找到目标id--------------------------------------------------------------------------------------------------------
        //为每个关键字树维护一个存有其右边界的List，用来在退栈时，找到相应的左边界，找到了左边界就将相应的右边界值在List中移除
        List<List<Integer>> allBoundList = new ArrayList<>(n);
        for(int i = 0;i < n;i++){
            allBoundList.add(new LinkedList<>());
        }
        int latestRightBound = -1;
        int nextTar = targetTree;
        while(targetId != Integer.MAX_VALUE){//当目标节点id为无穷大时，停止扫描
            //从targetTree以外的树中寻找目标id
            for(int tar = (targetTree + 1) % n;tar != targetTree;tar = (tar + 1) % n){
                stack = stacks.get(tar);
                tau_w = tokens.get(tar).getTau_w();
                k_w = tokens.get(tar).getK_w();
                List<Integer> preTagIds = tagList.get(tar);
                List<Integer> boundList = allBoundList.get(tar);
                boolean findRightBound = false;
                boolean findTarget = false;
                while(!findRightBound){//当找到了右边界就停止扫描
                    preNode = stack.peek();
                    if(targetId < preNode.node.id) {//该节点比目标节点id大，则向左走
                        node = preNode.node.left;
                        v = null;
                        if(node == null){
                            String l = SHA.HASHDataToString(tau_w + preNode.code + ",0");
                            v = TMap.get(l);
                            TMap.remove(l);
                        }
                        if (node == null && v == null) {//如果左边已没有节点，有两种情况，还需要更新，或者不需要再更新
                            if(preNode.dot != 0){//需要更新,创建新的节点，并将其入栈
                                preTagIds.add(preNode.node.id);//向左走，先将该节点id加入标签中
                                stackIds = copySubList(preNode.upd_ids,0,preNode.dot);
                                String tempCode = preNode.code + ",0";
                                if (stackIds.size() != 0) {
                                    int mid = (stackIds.size() - 1) / 2;
                                    int tempId = stackIds.get(mid).getId();
                                    String hash = stackIds.get(mid).getIdHash();
                                    stackIds.remove(mid);
                                    node = new KeywordTreeNode(tempId, hash, "", "");
                                    ToDOTreeNode DoNode = new ToDOTreeNode(tempId, hash);
                                    ToSCTreeNode ScNode = new ToSCTreeNode(tempId, "new");
                                    stack.add(new StackNode(node, DoNode, ScNode, stackIds, mid, tempCode));
                                }
                            } else {//左边不需要更新，则该节点即为其右边界
                                int tempId = preNode.node.id;
                                if (tempId >= latestRightBound) {
                                    latestRightBound = tempId;
                                    nextTar = tar;
                                }
                                boundList.add(tempId);
                                preNode.DONode.setState("r");
                                preNode.DONode.setId(tempId);
                                findRightBound = true;
                            }
                        } else {//如果左边不空，向左走
                            preTagIds.add(preNode.node.id);//向左走，先将该节点id加入标签中
                            String tempCode = preNode.code + ",0";
                            int tempId;
                            String tempHash;
                            if(v != null){//服务器没有保存该节点
//                                String[] data = v.split(",");
                                node = StringXor.deXor(SHA.HASHDataToString(k_w + tempCode), v);
                                tempId = node.id;
                                tempHash = node.idHash;
//                                if(data.length == 1){
//                                    node = new KeywordTreeNode(tempId, tempHash, "","");
//                                }else{
//                                    node = new KeywordTreeNode(tempId, tempHash, data[1],data[2]);
//                                }
                            }else{//服务器保存该节点
                                tempId = node.id;
                                tempHash = node.idHash;
                            }
                            //分两种情况，左边需要更新和不需要更新
                            ToDOTreeNode DoNode = new ToDOTreeNode(tempId, tempHash);
                            if(preNode.dot != 0){//需要更新
                                stackIds = copySubList(preNode.upd_ids,0,preNode.dot);
                                ToSCTreeNode ScNode = new ToSCTreeNode(tempId, "old");
                                int i = 0;
                                for (; i < stackIds.size(); i++) {
                                    if (stackIds.get(i).getId() > tempId) {
                                        break;
                                    }
                                }
                                stack.add(new StackNode(node, DoNode, ScNode, stackIds, i, tempCode));
                            }else{//不需要更新
                                stack.add(new StackNode(node, DoNode, tempCode));
                            }
                        }
                    }
                    /*向右走前注意事项：
                    1、若左边还有待更新节点，需要先将左边先更新完毕，并完成出栈再回到该节点，并将preNode里的各节点连接左孩子或增加左孩子哈希值。
                对于DoNode和ScNode，连接左孩子，对于node，连接左孩子和增加左哈希值。
                    2、若左边没有待更新节点，需要更新preNode中DoNode中左孩子哈希值，node中的左孩子哈希值，若有SCNode，也需要更新其左哈希值。
                    2、若右边有需要更新的节点，新的stackNode中，DONode需要有具体的id值，SCNode需要有id的哈希值。
                    3、若右边没有需要更新的节点，新的stackNode中，DONode需要有id的哈希值，SCNode为Null。
                    */
                    else{//该节点比目标节点id小或等于该节点，则向右走

                        //更新左侧
                        updLeftSide(preNode, tau_w, k_w);
                        //在向右走之前，先比较preTagIds里是否有比目标节点小于等于的id，有则可以直接退栈到比目标节点小的最大的那个id节点开始向右走
                        preTagIds.add(preNode.node.id);
                        int down = preTagIds.size() - 1;
                        int up = 0;
                        while(down > up){
                            int mid = (up + down) / 2;
                            if(preTagIds.get(mid) > targetId){
                                up = mid + 1;
                            }else if(preTagIds.get(mid) <= targetId){
                                down = mid;
                            }
                        }
                        //退栈到相应的中间节点
                        stackPopOperate(stack, preTagIds.get(down), boundList, tau_w, k_w);
                        preTagIds = preTagIds.subList(0,down);//更新preTagIds
                        /*向右走情形:
                        1、右边有节点，将右边节点入栈即可。入栈时要分两种情况，有更新和没有更新
                        2、右边没有节点，若还有更新，则添加节点到右边。若没有更新，则无法向右，需退回到preTagIds当前节点的前一个节点，即为右边界，当前节点为左边界，
                        如果preTagIds没有节点，则没有右边界。
                        */
                        preNode = stack.peek();
                        if(targetId == preNode.node.id) {//若等于该节点，标记找到了target
                            findTarget = true;
                        }
                        String tempCode = preNode.code + ",1";
                        node = preNode.node.right;
                        v = null;
                        if(node == null){
                            String l = SHA.HASHDataToString(tau_w + tempCode);
                            v = TMap.get(l);
                            TMap.remove(l);
                        }
                        int tempId;
                        String tempHash;
                        if(node != null || v != null){//右边有节点
                            if(node == null){
//                                String[] data = v.split(",");
                                node = StringXor.deXor(SHA.HASHDataToString(k_w + tempCode), v);
                                tempId = node.id;
                                tempHash = node.idHash;
//                                if(data.length == 1){
//                                    node = new KeywordTreeNode(tempId, tempHash, "","");
//                                }else{
//                                    node = new KeywordTreeNode(tempId, tempHash, data[1],data[2]);
//                                }
                            }else{
                                tempId = node.id;
                                tempHash = node.idHash;
                            }
                            ToDOTreeNode DoNode = new ToDOTreeNode(tempId, tempHash);
                            if(preNode.upd_ids != null && preNode.dot != preNode.upd_ids.size()){//需要更新
                                ToSCTreeNode ScNode = new ToSCTreeNode(tempId, "old");
                                stackIds = copySubList(preNode.upd_ids,preNode.dot, preNode.upd_ids.size());
                                int i = 0;
                                for (; i < stackIds.size(); i++) {
                                    if (stackIds.get(i).getId() > tempId) {
                                        break;
                                    }
                                }
                                stack.add(new StackNode(node, DoNode, ScNode, stackIds, i, tempCode));
                            }else{//不需要更新
                                stack.add(new StackNode(node, DoNode, tempCode));
                            }
                        }else{//右边没有节点
                            if(preNode.upd_ids != null && preNode.dot != preNode.upd_ids.size()){//右边有更新
                                stackIds = copySubList(preNode.upd_ids,preNode.dot, preNode.upd_ids.size());
                                int mid = (stackIds.size() - 1) / 2;
                                tempId = stackIds.get(mid).getId();
                                tempHash = stackIds.get(mid).getIdHash();
                                stackIds.remove(mid);
                                node = new KeywordTreeNode(tempId, tempHash, "", "");
                                ToDOTreeNode DoNode = new ToDOTreeNode(tempId, tempHash);
                                ToSCTreeNode ScNode = new ToSCTreeNode(tempId, "new");
                                stack.add(new StackNode(node, DoNode, ScNode, stackIds, mid, tempCode));
                            } else {//右边没有更新，即当前节点为左边界，preTagIds的上一个节点为右边界
                                preNode.DONode.setState("l");

//                                preNode.DONode.setId(preNode.node.id);
                                if(preTagIds.size() != 0){//如果还存在右边界
                                    tempId = preTagIds.get(preTagIds.size() - 1);
                                    stackPopOperate(stack, tempId, boundList, tau_w, k_w);
                                    preNode = stack.peek();
                                    preNode.DONode.setState("r");
//                                    preNode.DONode.setId(preNode.node.id);
                                    if (preNode.node.id >= latestRightBound) {
                                        latestRightBound = preNode.node.id;
                                        nextTar = tar;
                                    }
                                }else{//不存在右边界
                                    tempId = -1;//设置tempId，使stack出栈到空
                                    stackPopOperate(stack, tempId, boundList, tau_w, k_w);
                                    latestRightBound = Integer.MAX_VALUE;
                                    nextTar = tar;
                                }
                                findRightBound = true;
                            }
                        }
                    }
                }
                tagList.set(tar,preTagIds);//将最新的preTagIds更新到tagList
                if(!findTarget){//如果没有找到目标id，则将最大的右边界作为下一轮目标id
                    targetTree = nextTar;
                    targetId = latestRightBound;
                    break;
                } else if((tar + 1) % n == targetTree){//如果当前轮数是最后一轮，并且找到了目标id，则将该id加入结果
                    result.add(targetId);
                    targetTree = nextTar;//将最大的右边界做为下一轮的目标
                    targetId = latestRightBound;
                    break;
                }
            }
        }
        //-----------------------------------------------------------------------------------------------------------------------------------------

        //将所有树保存的栈出栈为空
        for(int i = 0;i < n;i++){
            stack = stacks.get(i);
            tau_w = tokens.get(i).getTau_w();
            k_w = tokens.get(i).getK_w();
            if(!stack.isEmpty()) {
                //更新栈顶元素左侧
                preNode = stack.peek();
                updLeftSide(preNode, tau_w, k_w);
                //出栈
                List<Integer> boundList = allBoundList.get(i);
                stackPopOperate(stack,-1,boundList,tau_w,k_w);
            }
        }
        return new SearchOutput(result, VOToDO, VOToSC);
    }


    //出栈操作，包括1、检查是否出栈节点为左边界。2、连接出栈节点与栈顶节点。3、更新出栈节点右侧。
    private void stackPopOperate(Stack<StackNode> stack, int targetId, List<Integer> boundList, String tau_w, String k_w){
        List<UpdData> stackIds;
        String v = null;
        while(!stack.isEmpty() && stack.peek().node.id != targetId){
            StackNode preNode = stack.pop();
            //如果退栈时，有节点比右边界列表最大值小，则为左边界
            if(boundList.size() != 0 && preNode.node.id < boundList.get(boundList.size() - 1)){
                preNode.DONode.setState("l");
//                preNode.DONode.setId(preNode.node.id);
                boundList.remove(boundList.size() - 1);
            }
            //若是从左侧退栈，退栈前需检查右侧是否还有更新,更新右侧时，只需返回其哈希值给DO端----------
            updRightSide(preNode, tau_w, k_w);
            //--------------------------------------------
            if(!stack.isEmpty()){
                //出栈后，还需连接栈顶节点的左孩子或右孩子
                StackNode peekNode = stack.peek();
                if(peekNode.node.id > preNode.node.id){//是栈顶节点的左孩子
                    peekNode.node.left = preNode.node;
                    peekNode.DONode.left = preNode.DONode;
                    if(peekNode.dot != 0) {//左边有更新，所以要重新计算哈希值
                        peekNode.node.leftHash = SHA.HASHDataToString(preNode.node.id + preNode.node.idHash + preNode.node.leftHash + preNode.node.rightHash);
                        peekNode.SCNode.left = preNode.SCNode;
                    }
                }else{//栈顶元素的右孩子
                    peekNode.node.right = preNode.node;
                    peekNode.DONode.right = preNode.DONode;
                    if(peekNode.upd_ids != null && peekNode.dot != peekNode.upd_ids.size()) {//右边有更新，所以要重新计算右哈希值
                        peekNode.node.rightHash = SHA.HASHDataToString( preNode.node.id + preNode.node.idHash + preNode.node.leftHash + preNode.node.rightHash);
                        peekNode.SCNode.right = preNode.SCNode;
                    }
                }
            }
        }
    }

    //更新右侧，右侧有更新更新即可，没有更新则更新传输给客户端的哈希值
    private void updRightSide(StackNode preNode, String tau_w, String k_w) {
        if(preNode.DONode.right == null && (preNode.SCNode == null || preNode.SCNode.right == null) && preNode.upd_ids != null && preNode.dot != preNode.upd_ids.size()){//确保右侧没有更新以后，更新右侧
            List<UpdData> stackIds = copySubList(preNode.upd_ids, preNode.dot, preNode.upd_ids.size());
            KeywordTreeNode node = preNode.node.right;
            String v = null;
            if(node == null){
                String l = SHA.HASHDataToString(tau_w + preNode.code + ",1");
                v = TMap.get(l);
                TMap.remove(l);
            }
            ToSCTreeNode ScNode;
            int i = 0;
            int tempId;
            String tempHash;
            if(node != null || v != null){//如果右侧不为空
                if(node == null){
//                    String[] data = v.split(",");
                    node = StringXor.deXor(SHA.HASHDataToString(k_w + preNode.code + ",1"), v);
                    tempId = node.id;
                    tempHash = node.idHash;
//                    if(data.length == 1){
//                        node = new KeywordTreeNode(tempId, tempHash, "","");
//                    }else{
//                        node = new KeywordTreeNode(tempId, tempHash, data[1],data[2]);
//                    }
                }else{
                    tempId = node.id;
                    tempHash = node.idHash;
                }
                ScNode = new ToSCTreeNode(tempId, "old");
                for (; i < stackIds.size(); i++) {
                    if (stackIds.get(i).getId() >= tempId) {
                        break;
                    }
                }
            }else{//如果右侧为空
                i = (stackIds.size() - 1) / 2;
                tempId = stackIds.get(i).getId();
                tempHash = stackIds.get(i).getIdHash();
                stackIds.remove(i);
                node = new KeywordTreeNode(tempId, tempHash, "", "");
                ScNode = new ToSCTreeNode(tempId, "new");
            }
            StackNode updRootNode = updOperate(new StackNode(node, null, ScNode, stackIds,i,preNode.code + ",1"),tau_w,k_w);
            preNode.node.right = updRootNode.node;
            preNode.node.rightHash = SHA.HASHDataToString(updRootNode.node.id + updRootNode.node.idHash + updRootNode.node.leftHash + updRootNode.node.rightHash);
            preNode.DONode.rightHash = preNode.node.rightHash;
            preNode.SCNode.right = updRootNode.SCNode;
        } else{//若右侧不需要更新
            if(preNode.DONode.right == null) {
                preNode.DONode.rightHash = preNode.node.rightHash;
            }
            if(preNode.SCNode != null && preNode.dot == preNode.upd_ids.size()){
                preNode.SCNode.rightHash = preNode.node.rightHash;
            }
        }
    }

    //更新左侧，左侧有更新更新即可，没有更新则更新传输给客户端的哈希值
    private void updLeftSide(StackNode preNode, String tau_w, String k_w) {
        //若左边还有待更新数据并且还没有更新，则先更新左边，没有数据需要更新左哈希值------------------
        if(preNode.DONode.left == null && (preNode.SCNode == null || preNode.SCNode.left == null) && preNode.dot != 0){
            List<UpdData> stackIds = copySubList(preNode.upd_ids,0,preNode.dot);
            String tempCode = preNode.code + ",0";
            String v = null;
            KeywordTreeNode node = preNode.node.left;
            if(node == null){   
                String l = SHA.HASHDataToString(tau_w + tempCode);
                v = TMap.get(l);
                TMap.remove(l);
            }
            ToSCTreeNode ScNode;
            int i = 0;
            int tempId;
            String tempHash;
            if(node != null || v != null){//如果左边不为空
                if(node == null){//服务器没有存储该节点
//                    String[] data = v.split(",");
                    node = StringXor.deXor(SHA.HASHDataToString(k_w + tempCode), v);
                    tempId = node.id;
                    tempHash = node.idHash;
//                    if(data.length == 1){
//                        node = new KeywordTreeNode(tempId, tempHash, "","");
//                    }else{
//                        node = new KeywordTreeNode(tempId, tempHash, data[1],data[2]);
//                    }
                }else{//服务器存储该节点
                    tempId = node.id;
                    tempHash = node.idHash;
                }
                ScNode = new ToSCTreeNode(tempId , "old");
                for (; i < stackIds.size(); i++) {
                    if (stackIds.get(i).getId() > tempId) {
                        break;
                    }
                }
            }else{//如果左边为空
                i = (stackIds.size() - 1) / 2;
                tempId = stackIds.get(i).getId();
                tempHash = stackIds.get(i).getIdHash();
                stackIds.remove(i);
                node = new KeywordTreeNode(tempId, tempHash, "", "");
                ScNode = new ToSCTreeNode(tempId , "new");
            }
            StackNode updRootNode = updOperate(new StackNode(node, null, ScNode, stackIds, i, tempCode), tau_w, k_w);
            preNode.node.left = updRootNode.node;
            preNode.node.leftHash = SHA.HASHDataToString(updRootNode.node.id + updRootNode.node.idHash + updRootNode.node.leftHash + updRootNode.node.rightHash);
            //更新左边后，只需要将其哈希值传给DO端。
            preNode.DONode.leftHash = preNode.node.leftHash;
            preNode.SCNode.left = updRootNode.SCNode;
        } else{//左边没有更新，更新哈希值
            if(preNode.DONode.left == null) {//如果左边没有返回客户端的节点，更新哈希值
                preNode.DONode.leftHash = preNode.node.leftHash;
            }
            if(preNode.SCNode != null && preNode.dot == 0){//如果还有更新节点，并且左边没有需要更新的节点，更新哈希值
                preNode.SCNode.leftHash = preNode.node.leftHash;
            }
        }
    }

    //更新仅需更新的节点，不需要有传给客户端的节点
    private StackNode updOperate(StackNode preNode, String tau_w, String k_w){
    //更新preNode的左边
        int tempId;
        String tempHash;
        if(preNode.dot == 0){//左边没有数据更新
            preNode.SCNode.leftHash = preNode.node.leftHash;
        }else{
            List<UpdData> stackIds = copySubList(preNode.upd_ids,0, preNode.dot);
            String v = null;
            KeywordTreeNode node = preNode.node.left;
            if(node == null){
                String l = SHA.HASHDataToString(tau_w + preNode.code + ",0");
                v = TMap.get(l);
                TMap.remove(l);
            }
            StackNode leftNode;
            //左边没有节点
            if(node == null && v == null){
                int mid = (stackIds.size() - 1) / 2;
                tempId = stackIds.get(mid).getId();
                tempHash = stackIds.get(mid).getIdHash();
                stackIds.remove(mid);
                node = new KeywordTreeNode(tempId, tempHash, "", "");
                ToSCTreeNode ScNode = new ToSCTreeNode(tempId,"new");
                leftNode = updOperate(new StackNode(node, null, ScNode, stackIds,mid,preNode.code + ",0"),tau_w,k_w);
            }
            //左边有节点
            else{
                if(node == null){
                    String[] data = v.split(",");
                    node = StringXor.deXor(SHA.HASHDataToString(k_w + preNode.code + ",0"), v);
                    tempId = node.id;
                    tempHash = node.idHash;
//                    if(data.length == 1){
//                        node = new KeywordTreeNode(tempId, tempHash, "","");
//                    }else{
//                        node = new KeywordTreeNode(tempId, tempHash, data[1],data[2]);
//                    }
                }else{
                    tempId = node.id;
                    tempHash = node.idHash;
                }
                ToSCTreeNode ScNode = new ToSCTreeNode(tempId,"old");
                int i = 0;
                for (; i < stackIds.size(); i++) {
                    if (stackIds.get(i).getId() > tempId) {
                        break;
                    }
                }
                leftNode = updOperate(new StackNode(node, null, ScNode, stackIds, i,preNode.code + ",0"),tau_w,k_w);
            }
            preNode.node.left = leftNode.node;
            preNode.node.leftHash = SHA.HASHDataToString(leftNode.node.id + leftNode.node.idHash + leftNode.node.leftHash + leftNode.node.rightHash);
            preNode.SCNode.left = leftNode.SCNode;
        }
    //更新preNode的右边
        if(preNode.dot == preNode.upd_ids.size()){//右边没有数据更新
            preNode.SCNode.rightHash = preNode.node.rightHash;
        }else{
            List<UpdData> stackIds = copySubList(preNode.upd_ids,preNode.dot, preNode.upd_ids.size());
            String v = null;
            KeywordTreeNode node = preNode.node.right;
            if(node == null){
                String l = SHA.HASHDataToString(tau_w + preNode.code + ",1");
                v = TMap.get(l);
                TMap.remove(l);
            }
            StackNode rightNode;
            //右边没有节点
            if(node == null && v == null){
                int mid = (stackIds.size() - 1) / 2;
                tempId = stackIds.get(mid).getId();
                tempHash = stackIds.get(mid).getIdHash();
                stackIds.remove(mid);
                node = new KeywordTreeNode(tempId, tempHash, "","");
                ToSCTreeNode ScNode = new ToSCTreeNode(tempId,"new");
                rightNode = updOperate(new StackNode(node, null, ScNode, stackIds, mid,preNode.code + ",1"), tau_w, k_w);
            }
            //右边有节点
            else{
                if(node == null){
//                    String[] data = v.split(",");
                    node = StringXor.deXor(SHA.HASHDataToString(k_w + preNode.code + ",1"), v);
                    tempId = node.id;
                    tempHash = node.idHash;
//                    if(data.length == 1){
//                        node = new KeywordTreeNode(tempId, tempHash, "","");
//                    }else{
//                        node = new KeywordTreeNode(tempId, tempHash, data[1],data[2]);
//                    }
                }else{
                    tempId = node.id;
                    tempHash = node.idHash;
                }
                ToSCTreeNode ScNode = new ToSCTreeNode(tempId, "old");
                int i = 0;
                for (; i < stackIds.size(); i++) {
                    if (stackIds.get(i).getId() > tempId) {
                        break;
                    }
                }
                rightNode = updOperate(new StackNode(node, null, ScNode, stackIds, i,preNode.code + ",1"),tau_w,k_w);
            }
            preNode.node.right = rightNode.node;
            preNode.node.rightHash = SHA.HASHDataToString(rightNode.node.id + rightNode.node.idHash + rightNode.node.leftHash + rightNode.node.rightHash);
            preNode.SCNode.right = rightNode.SCNode;
        }
        return preNode;
    }


    private void bipInsert(List<UpdData> upd_Ids, UpdData updData){
        int upd_Id = updData.getId();
        int l = 0,r = upd_Ids.size() - 1;
        while(l <= r){
            int mid = l + (r - l) / 2;
            if(upd_Ids.get(mid).getId() > upd_Id){
                r = mid - 1;
            }else{
                l = mid + 1;
            }
        }
        upd_Ids.add(l,updData);
    }

    private List<UpdData> copySubList(List<UpdData> list, int start, int end){
        List<UpdData> subList = new ArrayList<>();
        for(int i = start;i < end;i++){
            subList.add(list.get(i));
        }
        return subList;
    }


}
