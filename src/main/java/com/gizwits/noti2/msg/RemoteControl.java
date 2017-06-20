package com.gizwits.noti2.msg;

import java.util.List;
import java.util.Map;

public class RemoteControl {
    private String cmd;
    private RcData data;

    public RemoteControl(String cmd, String did, String mac, String productKey, List<Integer> raw) {
        this.cmd = cmd;
        this.data = new RcData(did, mac, productKey, raw);
    }

    public RemoteControl(String cmd, String did, String mac, String productKey, Map attrs) {
        this.cmd = cmd;
        this.data = new RcData(did, mac, productKey, attrs);
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public RcData getData() {
        return data;
    }

    public void setData(RcData data) {
        this.data = data;
    }
}

class RcData {
    private String did;
    private String mac;
    private String product_key;
    private List<Integer> raw;
    private Map attrs;

    public RcData(String did, String mac, String product_key, List<Integer> raw) {
        this.did = did;
        this.mac = mac;
        this.product_key = product_key;
        this.raw = raw;
    }

    public RcData(String did, String mac, String product_key, Map dp) {
        this.did = did;
        this.mac = mac;
        this.product_key = product_key;
        this.attrs = dp;
    }

    public String getDid() {
        return did;
    }

    public void setDid(String did) {
        this.did = did;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getProduct_key() {
        return product_key;
    }

    public void setProduct_key(String product_key) {
        this.product_key = product_key;
    }

    public Map getAttrs() {
        return attrs;
    }

    public void setAttrs(Map attrs) {
        this.attrs = attrs;
    }

    public List<Integer> getRaw() {
        return raw;
    }

    public void setRaw(List<Integer> raw) {
        this.raw = raw;
    }
}