package com.gizwits.noti2.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gizwits.noti2.client.HeartBeat;
import com.gizwits.noti2.client.NettyClient;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by Vincent on 2015/8/20.
 */
public class ClientHandler extends SimpleChannelInboundHandler<String> {
    /**noti2.0 收到事件回复格式*/
    static final String ACK = "{\"cmd\":\"event_ack\",\"delivery_id\":deliveryId}\n";

    private static final Logger logger = LogManager.getLogger();

    //heartbeat
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> scheduledFuture;

    private NettyClient nettyClient;

    public ClientHandler(NettyClient nettyClient) {
        this.nettyClient = nettyClient;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.warn("channel is inactive.");
        nettyClient.destroy();
        logger.warn("==>服务器挂掉了，重连");
        nettyClient.initConnect();
        nettyClient.login();
        super.channelInactive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        logger.info("channelMsg:" + msg);
        JSONObject object = JSON.parseObject(msg);

        String cmd = object.getString("cmd");
        switch (cmd) {
            case "login_res":
                handleLoginRes(object, ctx);
                break;
            case "pong":
                handlePong();
                break;
            case "remote_control_res":
                handleRemoteControlRes(object);
                break;
            case "event_push":
                handleEventPush(object, ctx);
                break;
            case "invalid_msg":
                handleInvalidMsg(object);
                break;
            default:
                logger.error("==>" + msg);
        }
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();

        if(this.scheduledFuture != null){
            this.scheduledFuture.cancel(true);
            this.scheduledFuture = null;
        }

        ctx.close();
    }

    private void handleLoginRes(JSONObject object, ChannelHandlerContext ctx) {
        JSONObject data = object.getJSONObject("data");
        boolean result = data.getBooleanValue("result");
        if (result) {
            this.scheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(new HeartBeat(this.nettyClient, ctx), 20, 20, TimeUnit.SECONDS);
            nettyClient.setLastHeartBeatTime(System.currentTimeMillis());
        }
        else {
            String errMsg = data.getString("msg");
            logger.error("==> login error: " + errMsg);
        }
    }

    private void handlePong()
    {
        nettyClient.setLastHeartBeatTime(System.currentTimeMillis());
    }

    private void handleRemoteControlRes(JSONObject object)
    {
        //TODO:
        return;
    }

    private void handleEventPush(JSONObject object, ChannelHandlerContext ctx) {
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
                    break;
                case "device_online":
                    logger.info("设备上线------------------{}------------------设备上线", object.getString("mac"));
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
    }

    private void handleInvalidMsg(JSONObject object)
    {
        String errMsg = object.getString("msg");
        logger.error("==> receive invalid msg:" + errMsg);
    }
}

