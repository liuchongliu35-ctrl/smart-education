package com.bing.tpa;

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.file.Paths;

@SpringBootApplication
@EnableScheduling
public class Main {
    public static void main(String[] args){
        SpringApplication.run(Main.class,args);
    }
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
