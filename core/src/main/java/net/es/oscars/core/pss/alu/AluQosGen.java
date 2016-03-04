package net.es.oscars.core.pss.alu;

import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.core.pss.PSSOperation;
import net.es.oscars.core.pss.ftl.AluQos;
import net.es.oscars.core.pss.ftl.util.Stringifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class AluQosGen {
    @Autowired
    private Stringifier stringifier;

    private String setupTpl = "alu-qos-setup.ftl";
    private String teardownTpl = "alu-qos-teardown.ftl";
    private String modifyTpl = "alu-qos-modify.ftl";


    public String generate(List<AluQos> qosList, boolean protect, boolean applyQos, String templateDir, PSSOperation op)
            throws IOException, TemplateException {

        String templateFile = setupTpl;
        switch (op) {
            case TEARDOWN:
                templateFile = teardownTpl;
                break;
            case SETUP:
                break;
            case MODIFY:
                break;
            default:
        }

        Map<String, Object> root = new HashMap<>();
        root.put("qosList", qosList);
        root.put("protect", protect);
        root.put("apply", applyQos);
        return stringifier.stringify(root, templateDir, templateFile);

    }
}
