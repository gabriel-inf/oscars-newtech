package net.es.oscars.pss.prop;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "getconfig")
@Data
@Component
@NoArgsConstructor
public class GetConfigProps {

    @NonNull
    private Boolean perform;

    @NonNull
    private String host;

    @NonNull
    private String path;
}


