package server;

import java.util.List;

public class SearchOutput {
    List<Integer> result;
    List<ToDOTreeNode> VOToDO;

    List<ToSCTreeNode> VOToSC;

    @Override
    public String toString() {
        return "searchOutput{" +
                "result=" + result +
                ", VOToDO=" + VOToDO +
                '}';
    }

    public SearchOutput(List<Integer> result, List<ToDOTreeNode> VOToDO, List<ToSCTreeNode> VOToSC) {
        this.result = result;
        this.VOToDO = VOToDO;
        this.VOToSC = VOToSC;
    }

    public SearchOutput() {

    }

    public List<Integer> getResult() {
        return result;
    }

    public List<ToDOTreeNode> getVOToDO() {
        return VOToDO;
    }
}
