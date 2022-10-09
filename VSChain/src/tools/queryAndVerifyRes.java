package tools;

public class queryAndVerifyRes {
    public long queryTime;
    public long verifyTime;
    public long VOSize;
    public boolean isPass;
    public queryAndVerifyRes(long queryTime, long verifyTime, long VOSize, boolean isPass) {
        this.queryTime = queryTime;
        this.verifyTime = verifyTime;
        this.VOSize = VOSize;
        this.isPass = isPass;
    }

    @Override
    public String toString() {
        return "queryAndVerifyRes{" +
                "queryTime=" + queryTime + "ns" +
                ", verifyTime=" + verifyTime + "ns" +
                ", VOSize=" + VOSize + "字节" +
                '}';
    }
}
