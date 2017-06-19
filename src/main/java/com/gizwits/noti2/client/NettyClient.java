package com.gizwits.noti2.client;

import com.alibaba.fastjson.JSON;
import com.gizwits.noti2.msg.BaseMsg;
import com.gizwits.noti2.msg.EventsEnum;
import com.gizwits.noti2.msg.LoginVO2;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class NettyClient {
    private static final Logger logger = LogManager.getLogger();

    private String host;
    private Integer port;

    private Channel channel;
    private EventLoopGroup group;

    private long lastHeartBeatTime;

    private String gizProductKey;
    private String authId;
    private String authSecret;
    private String subkey;
    private static final Integer threadCount = 20;

    public void initConnect(){

        group = new NioEventLoopGroup(threadCount);
        final Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ClientInitializer(this));
        try {
            logger.info("===>链接地址："+host + "端口="+port);
            ChannelFuture future = bootstrap.connect(host, port).sync();

            if(future.isSuccess()){
                logger.info("==> 链接服务器成功");
                channel = future.channel();
            }
            //channel.closeFuture().sync();
            // logger.info("==>close ");
        } catch (InterruptedException e) {
            logger.warn("== netty warn:", e);
        }
    }
    public void login(){
        LoginVO2 loginVO2 = new LoginVO2(gizProductKey, authId, authSecret, subkey, Arrays.asList(EventsEnum.STATUS_KV.getName(), EventsEnum.ONLINE.getName(), EventsEnum.OFFLINE.getName()));
        BaseMsg<LoginVO2> loginMsg = new BaseMsg<>("login_req", Arrays.asList(loginVO2));
        String msgStr = JSON.toJSONString(loginMsg)+"\n";
        logger.warn("==>登录"+msgStr);
        sendMsg(msgStr);
    }

    public void sendMsg(String msg){
        channel.writeAndFlush(msg);
    }

    public void init(String host, Integer port, String gizProductKey, String authId, String authSecret, String subkey){
        this.host = host;
        this.port = port;
        this.gizProductKey = gizProductKey;
        this.authId = authId;
        this.authSecret = authSecret;
        this.subkey = subkey;

        this.initConnect();//初始化服务器信息
        this.login();//登录服务器
    }
    public void destroy(){

        // heartbeat=false;
        if(channel.isOpen()){
            channel.close();
        }
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(!group.isShutdown()){
            group.shutdownGracefully();
        }
        logger.info("==>关闭m2m长链接");
    }

    public long getLastHeartBeatTime() {
        return lastHeartBeatTime;
    }

    public void setLastHeartBeatTime(long lastHeartBeatTime) {
        this.lastHeartBeatTime = lastHeartBeatTime;
    }
}

