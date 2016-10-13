package net.es.oscars.ui.prop;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Data
@Configuration
@ConfigurationProperties(prefix = "ui")
@NoArgsConstructor
public class UIProperties {
    @NonNull
    private File positionsFile;
}
