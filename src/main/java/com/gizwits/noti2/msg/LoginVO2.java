package com.gizwits.noti2.msg;

import java.util.List;

/**
 * Created by daitl on 2016/11/24.
 */
public class LoginVO2 {

    private String product_key;
    private String auth_id;
    private String auth_secret;
    private String subkey;
    private List<String> events;

    public LoginVO2(String product_key, String auth_id, String auth_secret, String subkey, List<String> events) {
        this.product_key = product_key;
        this.auth_id = auth_id;
        this.auth_secret = auth_secret;
        this.subkey = subkey;
        this.events = events;
    }

    public String getProduct_key() {
        return product_key;
    }

    public void setProduct_key(String product_key) {
        this.product_key = product_key;
    }

    public String getAuth_id() {
        return auth_id;
    }

    public void setAuth_id(String auth_id) {
        this.auth_id = auth_id;
    }

    public String getAuth_secret() {
        return auth_secret;
    }

    public void setAuth_secret(String auth_secret) {
        this.auth_secret = auth_secret;
    }

    public String getSubkey() {
        return subkey;
    }

    public void setSubkey(String subkey) {
        this.subkey = subkey;
    }

    public List<String> getEvents() {
        return events;
    }

    public void setEvents(List<String> events) {
        this.events = events;
    }
}

