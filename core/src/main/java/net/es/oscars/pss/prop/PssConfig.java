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
    private String url;

    @NonNull
    private String addressesFilename;

    @NonNull
    private String vcidRange;

    @NonNull
    private String aluSvcidRange;

    @NonNull
    private String aluSdpidRange;

    @NonNull
    private String aluQosidRange;

}
