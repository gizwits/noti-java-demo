package com.gizwits.noti2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Created by daitl on 2016/11/24.
 */
@SpringBootApplication
@EnableScheduling
public class Noti2Application {

    public static void main(String[] args) {
        SpringApplication.run(Noti2Application.class, args);
    }

}
