package com.gizwits.noti2.sslservice.msg;

/**
 * Created by Vincent on 2015/8/21.
 */
public class LoginVO {
    private String enterprise_id;
    private String enterprise_secret;
    private Integer prefetch_count;

    public LoginVO() {
    }

    public LoginVO(String enterprise_id, String enterprise_secret, Integer prefetch_count) {
        this.enterprise_id = enterprise_id;
        this.enterprise_secret = enterprise_secret;
        this.prefetch_count = prefetch_count;
    }

    public String getEnterprise_id() {
        return enterprise_id;
    }

    public void setEnterprise_id(String enterprise_id) {
        this.enterprise_id = enterprise_id;
    }

    public String getEnterprise_secret() {
        return enterprise_secret;
    }

    public void setEnterprise_secret(String enterprise_secret) {
        this.enterprise_secret = enterprise_secret;
    }

    public Integer getPrefetch_count() {
        return prefetch_count;
    }

    public void setPrefetch_count(Integer prefetch_count) {
        this.prefetch_count = prefetch_count;
    }
}
