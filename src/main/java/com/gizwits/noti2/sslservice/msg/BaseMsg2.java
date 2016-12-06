package com.gizwits.noti2.sslservice.msg;

import org.apache.poi.ss.formula.functions.T;

/**
 * Created by daitl on 2016/11/24.
 */
public class BaseMsg2 {

    private String cmd;
    private Integer prefetch;
    private T data;

    public BaseMsg2() {
    }

    public BaseMsg2(String cmd, T data) {
        this.cmd = cmd;
        this.data = data;
    }

    public BaseMsg2(String cmd, Integer prefetch, T data) {
        this.cmd = cmd;
        this.prefetch = prefetch;
        this.data = data;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public Integer getPrefetch() {
        return prefetch;
    }

    public void setPrefetch(Integer prefetch) {
        this.prefetch = prefetch;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
