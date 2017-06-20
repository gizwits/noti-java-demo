package com.gizwits;

import com.gizwits.noti2.client.NettyClient;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Main {

    private static NettyClient nettyClient;

    private static final String[] chars = new String[] { "a", "b", "c", "d", "e", "f",
            "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s",
            "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I",
            "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
            "W", "X", "Y", "Z" };

    public static void main(String[] args) {
        nettyClient = new NettyClient();

        // Set client info
        String notiHost = "snoti.gizwits.com";
        Integer notiPort = 2017;
        String productKey = "c74fd6e832eb42de80540d7d738fe025";
        String authId = "JxDB9Q20SNuqmPcJjth0Hw";
        String authSecret = "sIQBcW2NQh6rMzaz+3r3Iw";
        String subkey = "demo";
        nettyClient.init(notiHost, notiPort, productKey, authId, authSecret, subkey);

        // Remote Control
        String did = "8Namn3NCUNFRbuFiZ9NRaF";
        String mac = "virtual:site";
        String msgId = generateShortUuid();
        Map attrs = new HashMap();
        attrs.put("bool", true);
        attrs.put("enum", 1);
        attrs.put("number", 10);
        nettyClient.remoteControl(did, mac, productKey, attrs, msgId);

    }

    private static String generateShortUuid() {
        StringBuffer shortBuffer = new StringBuffer();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        for (int i = 0; i < 16; i++) {
            String str = uuid.substring(i * 2, i * 2 + 2);
            int x = Integer.parseInt(str, 16);
            shortBuffer.append(chars[x % 0x3E]);
        }
        return shortBuffer.toString();
    }
}
