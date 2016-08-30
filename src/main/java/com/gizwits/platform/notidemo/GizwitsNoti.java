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
import java.util.Map;
import java.util.HashMap;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.json.JSONObject;


public class GizwitsNoti 
{
    private static final Logger logger = LogManager.getLogger();
    private static final String GIZWITS_NOTI_HOST = "noti.gizwits.com"; // 机智云noti服务地址
    private static final int GIZWITS_NOTI_PORT = 2015;                  // 机智云noti ssl服务端口
    private String enterpriseId = "";                                   // 登录noti的企业id
    private String enterpriseSecret = "";                               // 登录noti的企业密钥
    private ReceiveThread receiveThread;                                // 接受socket报文的线程
    private SendThread sendThread;                                      // 向socket发送login，ping的线程
    private Socket socket;                                              // sslsocket对象
    private PrintWriter pw;                                             // socket的OutputStream字符流对象
    private boolean isConnect;                                          // socket连接状态
    private boolean isLogin;                                            // eid登录状态
    private int reconnCount;                                            // 重连次数
    private CallBack callBack;                                          // 接受处理设备通知的回调
    private final int MAXCONNECT = 2;                                   // 最大重连数
    private final int TIMEOUT = 10000;                                  // 等待接受socket消息超时时间
    
    public GizwitsNoti(String enterpriseId, String enterpriseSecret, CallBack callBack)
    {
        this.enterpriseId = enterpriseId;
        this.enterpriseSecret = enterpriseSecret;
        this.callBack = callBack;
    }
    
    public interface CallBack                                   // 接受设备消息的回调方法
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
        socket.setSoTimeout(TIMEOUT);                           // 设置socket的接受消息超时时间
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
                    reconnect();                                // 连接被断开重连
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            logger.debug("noti接口SendThread退出...." + Thread.currentThread().getName());
        }
        
        public void sendLoginMsg() throws IOException           
        {
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("enterprise_id", enterpriseId);
            data.put("enterprise_secret", enterpriseSecret);
            data.put("prefetch_count", 50);
            String msg = new JSONObject()
                            .put("cmd", "enterprise_login_req")
                            .put("data", data)
                            .toString();
            String sendMsg =  msg + "\n";
            sendMsg(sendMsg);                                   
            logger.debug("登录发送:" + sendMsg);
        }
        
        public void sendPingMsg() throws IOException, InterruptedException            
        {
            String sendMsg = "{\"cmd\": \"enterprise_ping\"}\n";
            sendMsg(sendMsg);
            logger.debug("发送心跳:" + sendMsg);
        }
        
        public void sendMsg(String sendMsg) throws IOException 
        {
            pw.write(sendMsg);                                  // 往socket写入消息
            pw.flush();                                         // 让socket发送已写入的消息
            socket.setSoTimeout(TIMEOUT);                       // 设置socket的接受消息超时时间 
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
                            	case "enterprise_login_res":    // 登录请求的返回
                                    checkLogin(json);
                                    break;
                            	case "enterprise_pong":         // ping指令的返回
                                    setPong();
                                    break;
                            	case "enterprise_event_push":   // 设备消息
                                    replyAck(json);
                                    callBack.call(json);
                                    break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                reader.close();
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                reconnect();                                    // 接受消息超时重连
            } catch (Exception e) {
                e.printStackTrace();
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
                    socket.setSoTimeout(0);                     // 设置接受消息超时时间为永久
                    reconnCount = 0;                            // 重置重连次数
                    logger.info("login success.");
                } else {
                    logger.info("login fail, msg: {}", data.getString("msg"));
                    disconnect();                               // 断开连接
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void setPong() throws SocketException
        {
            socket.setSoTimeout(0);                             // 设置接受消息超时时间为永久
        }

        private void replyAck(JSONObject json)                  // 回复noti服务端ack
        {
            try {
                String sendMsg = "{\"cmd\": \"enterprise_event_ack\",\"delivery_id\": " + json.getLong("delivery_id") + "}\n";
                logger.debug("发送ack:" + sendMsg);
                pw.write(sendMsg);
                pw.flush();
            } catch (Exception e) {
                e.printStackTrace();
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
                sendThread.join(3000);                          // 等待发送线程结束
                receiveThread.join(3000);                       // 等待接受线程结束
                if (reconnCount < MAXCONNECT) {
                    reconnCount++ ;
                    connect();                                  // 重新开始连接
                } 
            } catch (Exception e) {
                e.printStackTrace();
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
        }
    }
    
    private void disconnect()                                   // 断开socket连接
    {
        try {
            logger.debug("终止连接noti....");
            isConnect = false;
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
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
        
        new GizwitsNoti("8fb23e6dbf06438b8200cf4588e45b5f", "c7c9e01549004b96a8612a0e7c71a9d6", 
                        new CallBack() {
                            public void call(JSONObject msg)    
                            {
                                System.out.println( msg.toString() );
                            }
                        }).connect();
    }
}
