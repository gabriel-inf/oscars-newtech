package net.es.oscars.pss.svc;


import net.es.oscars.dto.pss.cmd.Command;
import net.es.oscars.dto.topo.enums.DeviceModel;
import net.es.oscars.pss.beans.ConfigException;
import net.es.oscars.pss.prop.PssConfig;
import net.es.oscars.pss.rancid.RancidArguments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RouterConfigBuilder {
    private PssConfig config;
    private AluCommandGenerator acg;

    @Autowired
    public RouterConfigBuilder(PssConfig config, AluCommandGenerator acg) {
        this.config = config;
        this.acg = acg;
    }

    public RancidArguments controlPlaneCheck(String device, DeviceModel model) throws ConfigException {
        String routerConfig;
        switch (model) {
            case ALCATEL_SR7750:
                routerConfig = "echo \"OSCARS PSS control plane check\"";
                break;
            case JUNIPER_MX:
            case JUNIPER_EX:
                routerConfig = "show chassis hardware";
                break;
            default:
                throw new ConfigException("unknown model");
        }

        return buildRouterConfig(routerConfig, device, model);

    }

    public RancidArguments setup(Command command) throws ConfigException {
        String routerConfig = "";
        DeviceModel model = command.getModel();
        switch (model) {
            case ALCATEL_SR7750:
                routerConfig = acg.setup(command.getAlu());
                break;
            case JUNIPER_MX:
            case JUNIPER_EX:
                break;
            default:
                throw new ConfigException("unknown model");
        }

        return buildRouterConfig(routerConfig, command.getDevice(), command.getModel());
    }


    public RancidArguments buildRouterConfig(String routerConfig, String device, DeviceModel model) throws ConfigException {
        String execPath;
        switch (model) {
            case ALCATEL_SR7750:
                execPath = config.getRancidDir() + "/alulogin";
                break;
            case JUNIPER_MX:
            case JUNIPER_EX:
                execPath = config.getRancidDir() + "/jlogin";
                break;
            default:
                throw new ConfigException("unknown model");
        }

        return RancidArguments.builder()
                .cloginrc(config.getCloginrc())
                .executable(execPath)
                .routerConfig(routerConfig)
                .router(device)
                .build();

    }

}
