package com.tokenmall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.tokenmall")
public class TokenMallApplication {

    public static void main(String[] args) {
        SpringApplication.run(TokenMallApplication.class, args);
    }
}
