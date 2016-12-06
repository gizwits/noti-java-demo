package com.gizwits.noti2.sslservice.client;

import com.alibaba.fastjson.JSON;
import com.gizwits.noti2.sslservice.msg.BaseMsg;
import com.gizwits.noti2.sslservice.msg.EventsEnum;
import com.gizwits.noti2.sslservice.msg.LoginVO2;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Created by Vincent on 2015/8/20.
 * 从新科项目直接拷贝过来，如有需要，请自行修改
 */
public class NettyClient {
    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);

    private  String	host	= "snoti.gizwits.com";
    private  Integer port	= 2017;

    private Channel channel;
    private EventLoopGroup group;
    
    private static final String GIZ_PRO_KEY = "";
    private static final String AUTH_ID = "";
    private static final String AUTH_SECRET = "";
    private static final String subkey = "QA";
    
    public void initConnect(){

        group = new NioEventLoopGroup(20);
        final Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ClientInitializer());
        try {
            logger.info("===>链接地址："+host + "端口="+port);
            ChannelFuture future = bootstrap.connect("snoti.gizwits.com",2017).sync();

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
       /* LoginVO login = new LoginVO(GIZ_ENTERPRISE_ID,GIZ_ENTERPRISE_SECRET,1);
        BaseMsg<LoginVO> loginMsg = new BaseMsg<>("enterprise_login_req",login);*/
        LoginVO2 loginVO2 = new LoginVO2(GIZ_PRO_KEY, AUTH_ID, AUTH_SECRET, subkey, Arrays.asList(EventsEnum.STATUS_KV.getName(), EventsEnum.ONLINE.getName(), EventsEnum.OFFLINE.getName()));
        BaseMsg<LoginVO2> loginMsg = new BaseMsg<>("login_req", Arrays.asList(loginVO2));
        String msgStr = JSON.toJSONString(loginMsg)+"\n";
        logger.warn("==>登录"+msgStr);
        sendMsg(msgStr);
    }

    public void sendMsg(String msg){
        channel.writeAndFlush(msg);
    }

    public void init(){
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


    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(Integer port) {
        this.port = port;
    }


  /*  public static void main(String[] args) {
        NettyClient nettyClient = new NettyClient();
        nettyClient.initConnect();
        nettyClient.login();
//        nettyClient.heartbeat();
    }*/
}
