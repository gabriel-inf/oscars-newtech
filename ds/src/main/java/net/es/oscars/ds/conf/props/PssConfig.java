package net.es.oscars.ds.conf.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@ConfigurationProperties(prefix = "pss")
@Data
@Component
public class PssConfig {
    public PssConfig() {

    }

    String defaultTemplateDir;


}
