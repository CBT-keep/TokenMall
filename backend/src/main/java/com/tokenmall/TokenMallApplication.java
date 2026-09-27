package com.tokenmall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.tokenmall")
@EnableScheduling // 开启定时任务
public class TokenMallApplication {

    public static void main(String[] args) {
        SpringApplication.run(TokenMallApplication.class, args);
    }
}
