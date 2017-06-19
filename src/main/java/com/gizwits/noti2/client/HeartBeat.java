package com.gizwits.noti2.client;

import io.netty.channel.ChannelHandlerContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HeartBeat implements Runnable {
    private static final Logger logger = LogManager.getLogger();

    private ChannelHandlerContext ctx;

    /**noti 2.0 收到心跳回复格式*/
    public static final String PING="{\"cmd\":\"ping\"}\n"; //心跳json

    private NettyClient nettyClient;

    public HeartBeat(NettyClient nettyClient, ChannelHandlerContext ctx){
        this.nettyClient = nettyClient;
        this.ctx = ctx;
    }

    @Override
    public void run() {

        logger.debug("==> heartbeat");
        long lastHeartBeat = nettyClient.getLastHeartBeatTime();
        if (System.currentTimeMillis() - lastHeartBeat > 3*1000*60) {
            nettyClient.destroy();
            logger.warn("==>服务器挂掉了，重连");
            nettyClient.initConnect();
            nettyClient.login();
        } else {
            ctx.writeAndFlush(PING);
        }

    }

}
