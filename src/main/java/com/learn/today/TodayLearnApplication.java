package com.learn.today;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.learn.today.**.mapper")
public class TodayLearnApplication {
    public static void main(String[] args) {
        SpringApplication.run(TodayLearnApplication.class, args);
    }
}
