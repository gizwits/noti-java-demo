package com.gizwits;

import com.gizwits.noti2.client.NettyClient;

public class Main {

    private static NettyClient nettyClient;

    public static void main(String[] args) {
        nettyClient = new NettyClient();
        String notiHost = Setting.getValue("gizwits.noti.host").trim();
        Integer notiPort = Integer.parseInt(Setting.getValue("gizwits.noti.port"));
        String productKey = Setting.getValue("gizwits.productKey").trim();
        String authId = Setting.getValue("gizwits.productKey.authId").trim();
        String authSecret = Setting.getValue("gizwits.productKey.authSecret").trim();
        String subkey = Setting.getValue("gizwits.productKey.subkey").trim();
        nettyClient.init(notiHost, notiPort, productKey, authId, authSecret, subkey);
    }
}
