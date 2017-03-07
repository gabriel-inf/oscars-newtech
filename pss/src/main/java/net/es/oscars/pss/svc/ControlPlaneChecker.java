package net.es.oscars.pss.svc;


import net.es.oscars.dto.topo.enums.DeviceModel;
import net.es.oscars.pss.beans.ControlPlaneException;
import net.es.oscars.pss.prop.PssConfig;
import net.es.oscars.pss.rancid.RancidArguments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ControlPlaneChecker {
    private PssConfig config;

    @Autowired
    public ControlPlaneChecker(PssConfig config) {
        this.config = config;
    }

    public RancidArguments controlPlaneCheck(String device, DeviceModel model) throws ControlPlaneException {
        String execPath;
        String command;
        switch (model) {
            case ALCATEL_SR7750:
                execPath = config.getRancidDir() + "/alulogin";
                command = "echo \"OSCARS PSS control plane check\"";
                break;
            case JUNIPER_MX:
            case JUNIPER_EX:
                execPath = config.getRancidDir() + "/jlogin";
                command = "show chassis hardware";
                break;
            default:
                throw new ControlPlaneException("unknown model");
        }

        return RancidArguments.builder()
                .cloginrc(config.getCloginrc())
                .executable(execPath)
                .command(command)
                .router(device)
                .build();

    }

}
