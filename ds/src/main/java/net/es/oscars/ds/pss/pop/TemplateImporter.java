package net.es.oscars.ds.pss.pop;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.ds.pss.prop.PssConfig;
import net.es.oscars.ds.pss.dao.TemplateRepository;
import net.es.oscars.ds.pss.ent.ETemplate;
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
    }

    public void importFromDir(boolean overwrite, String templateDir) {
        // do checks and read in new templates
        File folder = new File(templateDir);
        if (!folder.isDirectory()) {
            log.error(templateDir + " is not a directory! Skipping template import.");
            return;
        }

        List<ETemplate> newTemplates = new ArrayList<>();
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
                            String contents = FileUtils.readFileToString(file);
                            ETemplate tpl = new ETemplate(base);
                            tpl.setContents(contents);
                            log.info("Saving new template named " + base);
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

        List<ETemplate> templates = repository.findAll();

        if (templates.isEmpty()) {
            log.info("PSS template DB is empty. Importing new ones.");
            repository.save(newTemplates);

        } else {
            log.debug("PSS template DB not empty. Skipping template import.");
        }
    }
}
