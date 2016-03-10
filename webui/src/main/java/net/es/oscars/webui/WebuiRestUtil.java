package net.es.oscars.webui;

import net.es.oscars.rest.RestProperties;
import net.es.oscars.rest.RestTemplateBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@ComponentScan(basePackageClasses=net.es.oscars.rest.RestProperties.class)
public class WebuiRestUtil {

    @Autowired
    private RestProperties restProperties;

    @Bean
    public RestTemplate rest() throws Exception {
        return new RestTemplateBuilder().build(restProperties);
    }
}
