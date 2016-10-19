package net.es.oscars.tasks.prop;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "proc")
@NoArgsConstructor
public class ProcessingProperties {
    @NonNull
    private Integer timeoutHeldAfter;
}
