package net.es.oscars.ds.topo.pop;

import java.io.IOException;


public interface TopoImporter {


    void importFromFile(boolean overwrite, String devicesFilename, String adjciesFilename) throws IOException;
}
