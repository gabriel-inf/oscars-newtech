package net.es.oscars.pss;

import net.es.oscars.pss.pop.Startup;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties
@EnableAsync
@EnableScheduling
public class PssApp {
    public static void main(String[] args) {
        ConfigurableApplicationContext app = SpringApplication.run(PssApp.class, args);

        Startup startup = (Startup)app.getBean("startup");

        startup.onStart();



    }

}
