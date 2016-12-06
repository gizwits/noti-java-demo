package com.gizwits.noti2.sslservice.client;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.context.ServletContextAware;

import javax.annotation.Resource;
import javax.servlet.ServletContext;

/**
 * Created by Vincent on 2015/8/24.
 */
public class ServerInit implements InitializingBean, ServletContextAware {

    @Resource(name = "nettyClient")
    private NettyClient nettyClient;

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    @Override
    public void setServletContext(ServletContext context) {
        //测试服务器关闭长连接，正式服务器需要打开。
       nettyClient.init();
    }
}
