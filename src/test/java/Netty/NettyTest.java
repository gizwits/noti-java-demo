package Netty;

import com.gizwits.noti2.sslservice.client.NettyClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Created by daitl on 2016/11/24.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class NettyTest {

    @Autowired
    private NettyClient nettyClient;

    @Test
    public void testNetty() {
        nettyClient.init();
    }
}
