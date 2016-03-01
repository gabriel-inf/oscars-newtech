package net.es.oscars.webui;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.rest.RestTemplateBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@Slf4j
@SpringBootApplication
public class WebuiApp {

    public static void main(String[] args) {
        SpringApplication.run(WebuiApp.class, args);
    }

    @Bean
    public RestTemplate rest() throws Exception {
        return new RestTemplateBuilder().build();
   }
}