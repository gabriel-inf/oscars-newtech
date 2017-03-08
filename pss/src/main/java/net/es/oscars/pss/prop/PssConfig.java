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
    private String[] templateDirs;


    @NonNull
    private String checkFilename;


    @NonNull
    private String cloginrc;

    @NonNull
    private String rancidDir;

    @NonNull
    private String rancidHost;


    @NonNull
    private Boolean performGetconfig;

    @NonNull
    private String getconfigHost;

    @NonNull
    private String localGetconfigPath;
}


