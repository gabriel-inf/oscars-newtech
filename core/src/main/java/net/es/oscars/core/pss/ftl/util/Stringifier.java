package net.es.oscars.core.pss.ftl.util;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.RouterConfigTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

@Slf4j
@Component
public class Stringifier {
    private Configuration cfg;

    @Autowired
    private RestTemplate restTemplate;

    @PostConstruct
    public void postConstruct() {
        cfg = new Configuration(Configuration.VERSION_2_3_22);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setObjectWrapper(new DefaultObjectWrapper(Configuration.VERSION_2_3_22));
        cfg.setNumberFormat("computer");
        StringTemplateLoader stringLoader = new StringTemplateLoader();
        cfg.setTemplateLoader(stringLoader);

        String restPath = "https://localhost:8000/pss/templates/";
        String[] templateNames = restTemplate.getForObject(restPath, String[].class);
        for (String name : templateNames) {
            restPath = "https://localhost:8000/pss/templates/"+name;
            RouterConfigTemplate tpl = restTemplate.getForObject(restPath, RouterConfigTemplate.class);
            stringLoader.putTemplate(name, tpl.getContents());
            log.info("pulled template "+name);
        }
    }

    public String stringify(Map<String,Object> root, String templateFilename)
            throws IOException, TemplateException {

        Writer out = new StringWriter();

        Template tpl = cfg.getTemplate(templateFilename);
        tpl.process(root, out);
        out.flush();
        String output = out.toString();
        String[] lines = output.split("[\\r\\n]+");


        return String.join("\n", lines);

    }
}
