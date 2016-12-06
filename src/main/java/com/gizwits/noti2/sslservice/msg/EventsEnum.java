package com.gizwits.noti2.sslservice.msg;

/**
 * Created by daitl on 2016/11/24.
 */
public enum EventsEnum {

    ATTR_FAULT("device.attr_fault"),
    ATTR_ALERT("device.attr_alert"),
    ONLINE("device.online"),
    OFFLINE("device.offline"),
    STATUS_RAW("device.status.raw"),
    STATUS_KV("device.status.kv"),
    CHANGED("datapoints.changed");

    private String name;

    EventsEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
