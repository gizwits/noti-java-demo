package com.gizwits.noti2.sslservice.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Vincent on 2015/8/20.
 */
public class ClientHandler extends SimpleChannelInboundHandler<String> {
    /**noti1.0 收到事件回复格式*/
    /*static final String ACK = "{\"cmd\":\"enterprise_event_ack\",\"delivery_id\":deliveryId}\n";*///ack json

    /**noti2.0 收到事件回复格式*/
    static final String ACK = "{\"cmd\":\"event_ack\",\"delivery_id\":deliveryId}\n";

    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        logger.info("channelMsg:" + msg);
        JSONObject object = JSON.parseObject(msg);

        if (object.containsKey("delivery_id")) {
            logger.info("-------"+object.getString("delivery_id"));
            String ackStr = ACK.replace("deliveryId", object.getString("delivery_id"));
            //有需要回复的情况
            ctx.writeAndFlush(ackStr);
            //处理各种指令
            try {
                String event_type = object.getString("event_type");
                switch (event_type) {
                    case "device_offline":
                        logger.info("设备离线-----------------{}-------------------设备离线", object.getString("mac"));
                        logger.info("channelMsg:" + msg);
                        break;
                    case "device_online":

                        logger.info("设备上线------------------{}------------------设备上线", object.getString("mac"));
                        logger.info("channelMsg:" + msg);
                        break;
                    case "device_status_kv":
                        String mac = object.getString("mac");
                        String jsonData = object.getString("data");
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                logger.warn("==>解析异常", e);
            }

        } else {
            logger.info("==>" + msg);
        }
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
