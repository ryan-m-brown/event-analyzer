package com.masd.event;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.web.bind.annotation.RestController;

import org.mybatis.spring.annotation.MapperScan;

@MapperScan("com.masd.event.db")
@SpringBootApplication
@RestController
public class App {
    public static void main(String[] args) {
        new SpringApplicationBuilder(App.class).web(true).run(args);
    }
}
