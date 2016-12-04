package net.es.oscars;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties
public class WhatIfApp{
    public static void main(String[] args) {
        SpringApplication.run(WhatIfApp.class, args);
    }

}