package net.es.oscars.pss.pop;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.pss.dao.TemplateRepository;
import net.es.oscars.pss.ent.TemplateE;
import net.es.oscars.pss.prop.PssConfig;
import net.es.oscars.pss.tpl.Stringifier;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TemplateImporter {

    @Autowired
    private PssConfig pssConfig;

    @Autowired
    private TemplateRepository repository;


    @Autowired
    private Stringifier stringifier;


    @PostConstruct
    public void attemptImport() {
        log.info("Startup; if template DB is empty will import from directory set in pss.default-template-dir property .");
        if (pssConfig == null) {
            log.error("No pss stanza in application properties! Skipping template import.");
            return;
        } else if (pssConfig.getDefaultTemplateDir() == null) {
            log.error("Null pss.default-template-dir property! Skipping template import.");
            return;
        }
        String templateDir = pssConfig.getDefaultTemplateDir();
        this.importFromDir(false, templateDir);

        stringifier.initialize();

    }

    private void importFromDir(boolean overwrite, String templateDir) {
        log.info("attempting template import from "+templateDir);
        // do checks and read in new templates
        File folder = new File(templateDir);
        if (!folder.isDirectory()) {
            log.error(templateDir + " is not a directory! Skipping template import.");
            return;
        }

        List<TemplateE> newTemplates = new ArrayList<>();
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles == null) {
            log.error("Null folder contents for " + templateDir + ". Skipping template import.");
            return;
        } else {
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    String filename = file.getName();
                    String ext = FilenameUtils.getExtension(filename);
                    String base = FilenameUtils.getBaseName(filename);
                    String extension = pssConfig.getTemplateExtension();

                    if (ext.equals(extension)) {
                        try {
                            log.debug("Reading file: "+filename);
                            String contents = FileUtils.readFileToString(file);
                            TemplateE tpl = TemplateE.builder().name(base).contents(contents).build();
                            log.debug("Successfully read template named: " + base );
                            newTemplates.add(tpl);
                        } catch (IOException ex) {
                            log.error("Could not read template file from " + filename);
                        }
                    }
                }
            }
        }

        if (overwrite) {
            log.info("Overwrite set; deleting PSS template DB entries.");
            repository.deleteAll();
        }

        List<TemplateE> templates = repository.findAll();

        if (templates.isEmpty()) {
            log.info("PSS template DB is empty. Importing new ones.");
            repository.save(newTemplates);

        } else {
            log.debug("PSS template DB not empty. Skipping template import.");
        }
    }
}
