package net.es.oscars.webui;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Slf4j
@SpringBootApplication
@EnableConfigurationProperties
public class WebuiApp {

    public static void main(String[] args) {
        SpringApplication.run(WebuiApp.class, args);
    }


}