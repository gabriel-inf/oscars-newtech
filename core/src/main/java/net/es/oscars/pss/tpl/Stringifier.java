package net.es.oscars.pss.tpl;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.pss.dao.TemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class Stringifier {

    private Configuration cfg;

    @Autowired
    private TemplateRepository repo;

    public void initialize() {
        cfg = new Configuration(Configuration.VERSION_2_3_22);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setObjectWrapper(new DefaultObjectWrapper(Configuration.VERSION_2_3_22));
        cfg.setNumberFormat("computer");
        StringTemplateLoader stringLoader = new StringTemplateLoader();
        cfg.setTemplateLoader(stringLoader);

        repo.findAll().stream().forEach(
                t -> {
                    String name = t.getName();
                    String contents = t.getContents();
                    stringLoader.putTemplate(name, contents);
                    log.info("configured freemarker for template " + name);
                }
        );
    }

    public String stringify(Map<String, Object> root, String templateFilename) throws IOException, TemplateException {

        Writer writer = new StringWriter();

        Template tpl = cfg.getTemplate(templateFilename);
        tpl.process(root, writer);
        writer.flush();

        String output = writer.toString();
        List<String> lines = Arrays.asList(output.split("[\\r\\n]+"));

        return String.join("\n", lines);

    }
}
