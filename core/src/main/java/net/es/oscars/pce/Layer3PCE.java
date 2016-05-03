package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.pss.PCEAssistant;
import net.es.oscars.pss.PSSException;
import net.es.oscars.spec.ent.*;
import net.es.oscars.topo.ent.EDevice;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;

@Slf4j
@Component
public class Layer3PCE {


    public Layer3FlowE makeReserved(Layer3FlowE req_f, ScheduleSpecificationE scheduleSpec) throws PCEException {
        throw new PCEException("Layer 3 flows not supported yet");
    }



}
