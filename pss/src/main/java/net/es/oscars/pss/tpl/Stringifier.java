package net.es.oscars.pss.tpl;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
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
