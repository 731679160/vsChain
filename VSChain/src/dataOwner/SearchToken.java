package dataOwner;

public class SearchToken {
    String tau_w;
    String tau_upd_w;
    String k_w;
    String k_upd_w;

    public SearchToken(String tau_w, String tau_upd_w, String k_w, String k_upd_w) {
        this.tau_w = tau_w;
        this.tau_upd_w = tau_upd_w;
        this.k_w = k_w;
        this.k_upd_w = k_upd_w;
    }

    public String getTau_w() {
        return tau_w;
    }

    public void setTau_w(String tau_w) {
        this.tau_w = tau_w;
    }

    public String getTau_upd_w() {
        return tau_upd_w;
    }

    public void setTau_upd_w(String tau_upd_w) {
        this.tau_upd_w = tau_upd_w;
    }

    public String getK_w() {
        return k_w;
    }

    public void setK_w(String k_w) {
        this.k_w = k_w;
    }

    public String getK_upd_w() {
        return k_upd_w;
    }

    public void setK_upd_w(String k_upd_w) {
        this.k_upd_w = k_upd_w;
    }
}
