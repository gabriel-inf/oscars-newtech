package net.es.oscars.core;

import net.es.oscars.rest.RestProperties;
import net.es.oscars.rest.RestTemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableConfigurationProperties
@EnableAsync
@EnableScheduling
@ComponentScan(basePackageClasses=net.es.oscars.rest.RestProperties.class)
public class CoreApp {
    public static void main(String[] args) {
        SpringApplication.run(CoreApp.class, args);
    }

    @Autowired
    private RestProperties restProperties;

    @Bean
    public RestTemplate rest() throws Exception {
        return new RestTemplateBuilder().build(restProperties);
    }
}