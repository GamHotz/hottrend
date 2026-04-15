package com.hottrend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * HotTrend 主程序入口
 * 热点新闻聚合与分析工具
 */
@SpringBootApplication
@MapperScan("com.hottrend.mapper")
@EnableScheduling
@EnableRetry
public class HotTrendApplication {

    public static void main(String[] args) {
        SpringApplication.run(HotTrendApplication.class, args);
    }
}