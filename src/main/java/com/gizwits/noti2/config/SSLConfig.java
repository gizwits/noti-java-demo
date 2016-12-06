package com.gizwits.noti2.config;

import com.gizwits.noti2.sslservice.client.HeartBeat;
import com.gizwits.noti2.sslservice.client.NettyClient;
import com.gizwits.noti2.sslservice.client.ServerInit;
import com.gizwits.noti2.util.ApplicationUtil;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by daitl on 2016/9/21.
 */
@Configuration
public class SSLConfig {

    @Bean(autowire = Autowire.BY_NAME)
    public ApplicationUtil applicationUtil() {
        return new ApplicationUtil();
    }


    @Bean
    public NettyClient nettyClient() {
        NettyClient nettyClient = new NettyClient();
        nettyClient.setHost("snoti.gizwits.com");
        nettyClient.setPort(2017);
        return nettyClient;
    }

    @Bean
    public ServerInit serverInit() {
        return new ServerInit();
    }

    @Bean
    public HeartBeat heartBeat() {
        return new HeartBeat();
    }
}
