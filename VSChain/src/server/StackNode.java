package server;

import java.util.List;

public class StackNode {
    public ToDOTreeNode DONode;
    public ToSCTreeNode SCNode;
    public KeywordTreeNode node;
    public List<UpdData> upd_ids;
    public int dot;
    public String code;

    public StackNode(KeywordTreeNode node, ToDOTreeNode DONode, String code) {
        this.DONode = DONode;
        this.node = node;
        this.code = code;
    }

    public StackNode(KeywordTreeNode node, ToDOTreeNode DONode, ToSCTreeNode SCNode, List<UpdData> upd_ids, int dot, String code) {
        this.node = node;
        this.DONode = DONode;
        this.upd_ids = upd_ids;
        this.dot = dot;
        this.code = code;
        this.SCNode = SCNode;
    }
}
