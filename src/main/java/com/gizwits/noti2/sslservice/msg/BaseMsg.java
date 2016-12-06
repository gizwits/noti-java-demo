package com.gizwits.noti2.sslservice.msg;

import java.util.List;

/**
 * Created by l8611 on 2015/8/21.
 */
public class BaseMsg<T> {
    private String cmd;
    private List<T> data;

    public BaseMsg() {
    }

    public BaseMsg(String cmd, List<T> data) {
        this.cmd = cmd;
        this.data = data;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }
}
