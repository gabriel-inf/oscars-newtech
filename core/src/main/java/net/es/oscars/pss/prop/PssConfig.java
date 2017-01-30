package net.es.oscars.pss.prop;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "pss")
@Data
@Component
@NoArgsConstructor
public class PssConfig {

    @NonNull
    private String defaultTemplateDir;

    @NonNull
    private String templateExtension;

    @NonNull
    private String addressesFilename;

    @NonNull
    private String vcidRange;

    @NonNull
    private String sdpidRange;

    @NonNull
    private String qosidRange;

}
