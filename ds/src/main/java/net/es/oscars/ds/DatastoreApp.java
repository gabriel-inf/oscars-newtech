package net.es.oscars.ds;

import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Import;

@Import(DatastoreConfig.class)
public class DatastoreApp {
    public static void main(String[] args) {
        SpringApplication.run(DatastoreApp.class, args);
    }


}