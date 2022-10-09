package server;

public class UpdData {
    private int id;
    private String idHash;
    private String op;

    public UpdData() {
    }

    public UpdData(int id, String idHash, String op) {
        this.id = id;
        this.idHash = idHash;
        this.op = op;
    }

    public int getId() {
        return id;
    }

    public String getIdHash() {
        return idHash;
    }

    public String getOp() {
        return op;
    }
}
