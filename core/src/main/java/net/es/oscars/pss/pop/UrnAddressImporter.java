package net.es.oscars.pss.pop;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.helpers.JsonHelper;
import net.es.oscars.pss.dao.UrnAddressRepository;
import net.es.oscars.pss.ent.UrnAddressE;
import net.es.oscars.topo.prop.TopoProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class UrnAddressImporter {

    @Autowired
    private TopoProperties topoProperties;

    @Autowired
    private UrnAddressRepository repo;

    @Autowired
    private JsonHelper jsonHelper;


    @PostConstruct
    public void attemptImport() {

        log.info("importing IP addresses for URNs");
        String addrsFilename = "./config/topo/"+topoProperties.getPrefix()+"-addrs.json";
        try {
            List<UrnAddressE> addrs = importAddrsFromFile(addrsFilename);
            repo.deleteAll();
            repo.save(addrs);

            Integer num = addrs.size();
            log.info("imported "+num+" URN addresses");


        } catch (IOException e) {
            log.error(e.getMessage());
        }

    }
    private List<UrnAddressE> importAddrsFromFile(String filename) throws IOException {
        File jsonFile = new File(filename);
        ObjectMapper mapper = jsonHelper.mapper();
        return Arrays.asList(mapper.readValue(jsonFile, UrnAddressE[].class));
    }

}
