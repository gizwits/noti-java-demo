package com.gizwits.platform.notidemo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.json.JSONObject;
import org.json.JSONArray;


public class GizwitsNoti 
{
    private static final Logger logger = LogManager.getLogger();
    private static final String GIZWITS_NOTI_HOST = "noti2.gizwits.com";    // 机智云noti2服务地址
    private static final int GIZWITS_NOTI_PORT = 2016;                      // 机智云noti2 ssl服务端口
    private JSONArray products;                                             // 登录noti2的product信息
    private ReceiveThread receiveThread;                                    // 接收socket报文的线程
    private SendThread sendThread;                                          // 向socket发送login，ping的线程
    private Socket socket;                                                  // sslsocket对象
    private PrintWriter pw;                                                 // socket的OutputStream字符流对象
    private boolean isConnect;                                              // socket连接状态
    private boolean isLogin;                                                // 登录状态
    private int reconnCount;                                                // 重连次数
    private CallBack callBack;                                              // 接收处理设备通知的回调
    private final int MAXCONNECT = 720;                                     // 最大重连数
    private final int TIMEOUT = 10000;                                      // 等待接收socket消息超时时间
    
    public GizwitsNoti(JSONArray products, CallBack callBack)
    {
        this.products = products;
        this.callBack = callBack;
    }
    
    public interface CallBack                                   // 接收设备消息的回调方法
    {          
    	public abstract void call(JSONObject msg);
    }
    
    private Socket createSslSocket() throws IOException, NoSuchAlgorithmException, KeyManagementException
    {
    	SSLContext context = SSLContext.getInstance("SSL");
        context.init(null,                                      // 初始化，不发送客户端证书，也不验证服务端证书
                    new TrustManager[]{new MyX509TrustManager()},
                    new SecureRandom());
        SSLSocketFactory fcty = context.getSocketFactory();
        SSLSocket socket = (SSLSocket) fcty.createSocket(GIZWITS_NOTI_HOST, GIZWITS_NOTI_PORT);
        socket.setKeepAlive(true);                              // 开启socket的保活
        socket.setSoTimeout(TIMEOUT);                           // 设置socket的接收消息超时时间
        isConnect = true;
        return socket;
    }
    
    private class MyX509TrustManager implements X509TrustManager 
    {
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException 
        {
        }

        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException 
        {
        }

        public X509Certificate[] getAcceptedIssuers() 
        {
        	return null;
        }
    }
    
    private class SendThread extends Thread
    {
        private boolean hasSendLogin = false;                   // 是否发送登录指令
            
        @Override
        public void run() 
        {
            while (isConnect) {
                try {
                    if (!isLogin) {
                        if (!hasSendLogin) {
                            sendLoginMsg();
                            hasSendLogin = true;
                        } else {
                            Thread.sleep(1000);                 // 等待登录结果
                        }
                    } else {
                        for (int i = 0; i < 60; i++) {          // 1秒钟检查一次连接状态，1分钟发送一次ping指令
                            Thread.sleep(1000);
                            if (!isConnect) {
                            	logger.debug("noti接口SendThread退出...." + Thread.currentThread().getName());
                                return;
                            }
                        }
                        sendPingMsg();
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                    logger.error("exception: {}", e.toString());
                    reconnect();                                // 连接被断开重连
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("exception: {}", e.toString());
                }
            }
            logger.debug("noti接口SendThread退出...." + Thread.currentThread().getName());
        }
        
        public void sendLoginMsg() throws IOException           
        {
            String msg = new JSONObject()
                            .put("cmd", "login_req")
                            .put("prefetch_count", 50)
                            .put("data", products)
                            .toString();
            String sendMsg = msg + "\n";
            sendMsg(sendMsg);                                   
            logger.debug("登录发送:" + sendMsg);
        }
        
        public void sendPingMsg() throws IOException
        {
            String sendMsg = "{\"cmd\": \"ping\"}\n";
            sendMsg(sendMsg);
            logger.debug("发送心跳:" + sendMsg);
        }
        
        public void sendMsg(String sendMsg) throws IOException 
        {
            pw.write(sendMsg);                                  // 往socket写入消息
            pw.flush();                                         // 让socket发送已写入的消息
            socket.setSoTimeout(TIMEOUT);                       // 设置socket的接收消息超时时间 
        }
    }
    
    private class ReceiveThread extends Thread 
    {
        @Override
        public void run() 
        {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                while (isConnect) {
                    String line = reader.readLine();            // 读取socket消息
                    if (line != null) {
                        logger.debug("收到机智云通知: " + line);
                        try {
                            JSONObject json = new JSONObject(line);
                            String cmd = json.getString("cmd");
                            switch (cmd) 
                            {
                            	case "login_res":               // 登录请求的返回
                                    checkLogin(json);
                                    break;
                            	case "pong":                    // ping指令的返回
                                    setPong();
                                    break;
                            	case "event_push":              // 设备消息
                                    replyAck(json);
                                    callBack.call(json);
                                    break;
                                case "invalid_msg":
                                    callBack.call(json);
                                    int errorCode = json.getInt("error_code");
                                    if(4000 == errorCode) {     // noti2服务端内部错误
                                        reconnect();
                                    } else {                    // noti2客户端错误
                                        disconnect();
                                    }
                                    break;    
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            logger.error("exception: {}", e.toString());
                        }
                    }
                }
                reader.close();
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                logger.error("exception: {}", e.toString());
                reconnect();                                    // 接收消息超时重连
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("exception: {}", e.toString());
                disconnect();
            }
            logger.debug("noti接口ReceiveThread退出...." + Thread.currentThread().getName());
        }
        
        private void checkLogin(JSONObject json)
        {
            try {
                JSONObject data = json.getJSONObject("data");
                boolean result = data.getBoolean("result");
                if(result) {
                    isLogin = true;                             // 登录成功
                    socket.setSoTimeout(0);                     // 设置接收消息超时时间为永久
                    reconnCount = 0;                            // 重置重连次数
                    logger.info("login success.");
                } else {
                    logger.info("login fail, msg: {}", data.getString("msg"));
                    disconnect();                               // 登录失败断开连接
                }
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("exception: {}", e.toString());
                disconnect();                                   // 异常断开连接
            }
        }

        private void setPong() throws SocketException
        {
            socket.setSoTimeout(0);                             // 设置接收消息超时时间为永久
        }

        private void replyAck(JSONObject json)                  // 回复noti服务端ack
        {
            try {
                String sendMsg = "{\"cmd\": \"event_ack\",\"delivery_id\": " + json.getLong("delivery_id") + "}\n";
                logger.debug("发送ack:" + sendMsg);
                pw.write(sendMsg);
                pw.flush();
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("exception: {}", e.toString());
            }
        }
    }
    
    private class ReconnectThread extends Thread 
    {
        @Override
        public void run() 
        {
            try {
                Thread.sleep(5000);
                logger.debug("开始执行重连...");
                disconnect();
                if (sendThread != null) {
                    sendThread.join(3000);                      // 等待发送线程结束
                }
                if (sendThread != null) {
                    receiveThread.join(3000);                   // 等待接收线程结束
                }    
                if (reconnCount < MAXCONNECT) {
                    reconnCount++ ;
                    connect();                                  // 重新开始连接
                } 
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("exception: {}", e.toString());
            }
        }
    }
    
    public void connect()                                       // 开启连接
    {
        try {
            logger.debug("开始连接noti...");
            isConnect = false;
            isLogin = false;
            socket = createSslSocket();
            pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            sendThread = new SendThread();
            sendThread.start();
            receiveThread = new ReceiveThread();
            receiveThread.start();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("exception: {}", e.toString());
            reconnect();
        }
    }
    
    public void disconnect()                                    // 断开socket连接
    {
        try {
            logger.debug("终止连接noti....");
            if (isConnect) {
                isConnect = false;
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("exception: {}", e.toString());
        }
    }
    
    private void reconnect()                                    // 重连socket
    {
        logger.debug("准备执行重连...");
        new ReconnectThread().start();
    }
    
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        
        String productKey = "2b336927de044008a237620053d0c88b";
        String productSecret = "7e99e1096a034139ba3ba5556205516a";
        String subkey = "demo";
        String[] events = {"device.attr_fault", "device.attr_alert", "device.online", "device.offline", "device.status.raw", "device.status.kv", "datapoints.changed"};
        
        JSONObject product = new JSONObject()
                                .put("product_key", productKey)
                                .put("product_secret", productSecret)
                                .put("subkey", subkey)
                                .put("events", events);
        JSONArray products = new JSONArray().put(product);                                
        
        new GizwitsNoti(products, 
                        new CallBack() {
                            public void call(JSONObject msg)    
                            {
                                System.out.println( msg.toString() );
                            }
                        }).connect();
    }
}
