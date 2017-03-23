package net.es.oscars.pss.prop;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "startup")
@Data
@Component
@NoArgsConstructor
public class StartupProps {


    @NonNull
    private String[] templateDirs;

    @NonNull
    private Boolean performControlPlaneCheck;

    @NonNull
    private String controlPlaneCheckFilename;

}


