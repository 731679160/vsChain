package dataOwner;

import java.util.List;

public class InvertedIndex {
    private int keyword;
    private List<Integer> ids;

    public InvertedIndex(int keyword, List<Integer> ids){
        this.keyword = keyword;
        this.ids = ids;
    }

    public void addId(int id){
        this.ids.add(id);
    }

    public boolean deleteId(int id){
        for(int i = 0;i < ids.size();i++){
            if(ids.get(i) == id){
                ids.remove(i);
                return true;
            }
        }
        return false;
    }

    public int getKeyword() {
        return keyword;
    }

    public void setKeyword(int keyword) {
        this.keyword = keyword;
    }

    public List<Integer> getIds() {
        return ids;
    }

    public void setIds(List<Integer> ids) {
        this.ids = ids;
    }
}
