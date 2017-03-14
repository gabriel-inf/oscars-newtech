package net.es.oscars;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.pss.pop.UrnAddressImporter;
import net.es.oscars.topo.pop.TopoFileImporter;
import net.es.oscars.ui.pop.UIPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class Startup {

    private TopoFileImporter importer;
    private UIPopulator uiPopulator;
    private UrnAddressImporter urnAddressImporter;

    @Autowired
    public Startup(TopoFileImporter importer, UIPopulator populator, UrnAddressImporter urnAddressImporter) {

        this.importer = importer;
        this.uiPopulator = populator;
        this.urnAddressImporter = urnAddressImporter;
    }

    public void onStart() throws IOException {
        importer.startup();
        uiPopulator.startup();
        urnAddressImporter.startup();

    }


}
