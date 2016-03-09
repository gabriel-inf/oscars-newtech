package net.es.oscars.ds.pss.pop;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.ds.conf.props.PssConfig;
import net.es.oscars.ds.pss.dao.TemplateRepository;
import net.es.oscars.ds.pss.ent.ETemplate;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOExceptionWithCause;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class TemplatePopulator {

    @Autowired
    private PssConfig pssConfig;

    @Autowired
    private TemplateRepository repository;

    @PostConstruct
    public void fill() throws IOException {

        List<ETemplate> templates = repository.findAll();

        if (templates.isEmpty()) {
            if (pssConfig == null) {
                log.error("No PSS config!");
                return;
            } else if (pssConfig.getDefaultTemplateDir() == null) {
                log.error("Null default template dur!");
                return;

            }

            File folder = new File(pssConfig.getDefaultTemplateDir());
            File[] listOfFiles = folder.listFiles();
            if (listOfFiles == null) {
                log.error("no files at pss.defaultTemplateDir!");
            } else {
                for (File file : listOfFiles) {
                    if (file.isFile()) {
                        String filename = file.getName();
                        String ext = FilenameUtils.getExtension(filename);
                        String base = FilenameUtils.getBaseName(filename);
                        if (ext.equals("ftl")) {
                            log.info("saving new template from "+filename);
                            String contents = FileUtils.readFileToString(file);
                            ETemplate tpl = new ETemplate(base);
                            tpl.setContents(contents);
                            repository.save(tpl);
                        }
                    }
                }
            }
        } else {
            log.info("db not empty");
        }
    }
}
