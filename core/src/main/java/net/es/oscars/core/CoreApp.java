package net.es.oscars.core;

import net.es.oscars.rest.RestTemplateBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class CoreApp {

    public static void main(String[] args) {
        SpringApplication.run(CoreApp.class, args);
    }
    @Bean
    public RestTemplate rest() throws Exception {
        return new RestTemplateBuilder().build();
    }
}