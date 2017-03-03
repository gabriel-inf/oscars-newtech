package net.es.oscars.pss.alu;


import net.es.oscars.pss.prop.PssConfig;
import net.es.oscars.pss.rancid.RancidArguments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AluControlPlaneCheck {
    private PssConfig config;

    @Autowired
    public AluControlPlaneCheck(PssConfig config) {
        this.config = config;
    }

    public RancidArguments check(String device) {
        String execPath = config.getRancidDir()+"/alulogin";

        RancidArguments args = RancidArguments.builder()
                .cloginrc(config.getCloginrc())
                .useFile(false)
                .executable(execPath)
                .command("echo \"OSCARS PSS control plane check\"")
                .router(device)
                .build();

        return args;


    }

}
