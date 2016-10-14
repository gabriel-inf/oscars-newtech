package net.es.oscars.helpers;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class JsonHelper {
    ObjectMapper mapper = new ObjectMapper();
    public ObjectMapper mapper() {
        return mapper;
    }
}
