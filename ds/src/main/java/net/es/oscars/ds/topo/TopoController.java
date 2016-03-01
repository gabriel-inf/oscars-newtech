package net.es.oscars.ds.topo;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.common.topo.Layer;
import net.es.oscars.ds.topo.dao.TopologyRepository;
import net.es.oscars.ds.topo.dao.DeviceRepository;
import net.es.oscars.ds.topo.ent.EDevice;
import net.es.oscars.ds.topo.ent.EIfce;
import net.es.oscars.ds.topo.ent.ETopology;
import net.es.oscars.ds.topo.ent.EUrnAdjcy;
import net.es.oscars.dto.topo.Metric;
import net.es.oscars.dto.topo.TopoEdge;
import net.es.oscars.dto.topo.TopoVertex;
import net.es.oscars.dto.topo.Topology;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

@Slf4j
@Controller
public class TopoController {

    @Autowired
    private TopologyRepository topoRepo;

    @Autowired
    private DeviceRepository devRepo;

    @Autowired
    private ModelMapper modelMapper;


    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public void handleResourceNotFoundException(NoSuchElementException ex) {
        log.warn(ex.getMessage(), ex);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(value = HttpStatus.CONFLICT)
    public void handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.warn(ex.getMessage(), ex);
    }


    @RequestMapping(value = "/device/{urn}", method = RequestMethod.GET)
    @ResponseBody
    public EDevice device(@PathVariable("urn") String urn) {
        EDevice eDevice = devRepo.findByUrn(urn).orElseThrow(NoSuchElementException::new);
        log.info(eDevice.toString());
        return eDevice;
    }


    @RequestMapping(value = "/topo/{name}/layer/{layer}", method = RequestMethod.GET)
    @ResponseBody
    public Topology layer(@PathVariable("name") String name, @PathVariable("layer") String layer) {

        log.info("topology " + name + " layer " + layer);
        Layer eLayer = Layer.get(layer).orElseThrow(NoSuchElementException::new);
        ETopology etopo = topoRepo.findByName(name).orElseThrow(NoSuchElementException::new);


        Topology topo = new Topology();
        topo.setLayer(eLayer);

        etopo.getDevices().stream()
                .filter(d -> d.getCapabilities().contains(eLayer))
                .forEach(d -> {

                    TopoVertex dev = new TopoVertex(d.getUrn());
                    topo.getVertices().add(dev);

                    d.getIfces().stream()
                            .filter(i -> i.getCapabilities().contains(eLayer))
                            .forEach(i -> {
                                TopoVertex ifce = new TopoVertex(i.getUrn());
                                topo.getVertices().add(ifce);

                                TopoEdge edge = new TopoEdge(d.getUrn(), i.getUrn());
                                Metric m = new Metric();
                                m.setLayer(eLayer);
                                m.setValue(1L);
                                edge.getMetrics().add(m);
                                topo.getEdges().add(edge);
                            });
                });

        for (EUrnAdjcy adj : etopo.getAdjcies()) {
            Set<Metric> metrics = new HashSet<>();

            adj.getMetrics().stream()
                    .filter(em -> em.getLayer().equals(eLayer))
                    .forEach(em -> {
                        Metric m = new Metric();
                        m.setLayer(eLayer);
                        m.setValue(em.getValue());
                        metrics.add(m);
                    });
            if (!metrics.isEmpty()) {
                TopoEdge edge = new TopoEdge(adj.getA(), adj.getZ());
                edge.setMetrics(metrics);
                topo.getEdges().add(edge);
            }
        }


        return topo;
    }


}