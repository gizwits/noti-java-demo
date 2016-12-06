package com.gizwits.noti2.sslservice.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * Created by Vincent on 2015/9/10.
 */
public class HeartBeat {
    private final static Logger logger = LoggerFactory.getLogger(HeartBeat.class);
    /**noti 1.0 收到心跳回复格式*/
   /* public static final String PING="{\"cmd\":\"enterprise_ping\"}\n"; *///心跳json

    /**noti 2.0 收到心跳回复格式*/
    public static final String PING="{\"cmd\":\"ping\"}\n"; //心跳json

    @Resource(name = "nettyClient")
    private NettyClient nettyClient;
    @Scheduled(cron="0 */4 * * * ?")
    public void heartbeat(){
        try {
            TimeUnit.SECONDS.sleep(10);
            logger.debug("==> 心跳");
            if(System.currentTimeMillis()- 0l>3*1000*60){
                nettyClient.destroy();
                logger.warn("==>服务器挂掉了，重连");
                nettyClient.init();
            }else {
                nettyClient.sendMsg(PING);
                logger.warn("==发送心跳");
            }

        } catch (InterruptedException e) {
            // e.printStackTrace();
        }


    }
}
