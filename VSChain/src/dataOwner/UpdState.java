package dataOwner;

public class UpdState {
    public int tokenCnt;
    public int updCnt;
    public String updHash;
    public boolean isSearch;
    public UpdState(int tokenCnt, int updCnt, String updHash, boolean isSearch){
        this.tokenCnt = tokenCnt;
        this.updCnt = updCnt;
        this.updHash = updHash;
        this.isSearch = isSearch;
    }

    public int getTokenCnt() {
        return tokenCnt;
    }

    public void setTokenCnt(int tokenCnt) {
        this.tokenCnt = tokenCnt;
    }

    public int getUpdCnt() {
        return updCnt;
    }

    public void setUpdCnt(int updCnt) {
        this.updCnt = updCnt;
    }

    public String getUpdHash() {
        return updHash;
    }

    public void setUpdHash(String updHash) {
        this.updHash = updHash;
    }

    public boolean isUpd() {
        return isSearch;
    }

    public void setUpd(boolean isSearch) {
        isSearch = isSearch;
    }
}
