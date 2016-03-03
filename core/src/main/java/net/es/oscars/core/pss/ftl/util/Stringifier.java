package net.es.oscars.core.pss.ftl.util;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

@Component
public class Stringifier {

    public String stringify(Map<String,Object> root, String templateDir, String templateFilename)
            throws IOException, TemplateException {

        Configuration cfg = new Configuration(Configuration.VERSION_2_3_22);
        cfg.setDirectoryForTemplateLoading(new File(templateDir));
        cfg.setDefaultEncoding("UTF-8");
        cfg.setObjectWrapper(new DefaultObjectWrapper(Configuration.VERSION_2_3_22));
        cfg.setNumberFormat("computer");

        Writer out = new StringWriter();

        Template tpl = cfg.getTemplate(templateFilename);
        tpl.process(root, out);
        out.flush();
        String output = out.toString();
        String[] lines = output.split("[\\r\\n]+");


        return String.join("\n", lines);

    }
}
