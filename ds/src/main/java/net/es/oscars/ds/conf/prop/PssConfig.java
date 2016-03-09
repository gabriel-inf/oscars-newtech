package net.es.oscars.ds.conf.prop;

import lombok.Data;
import lombok.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "pss")
@Data
@Component
public class PssConfig {
    public PssConfig() {

    }

    @NonNull
    private String defaultTemplateDir;


}
