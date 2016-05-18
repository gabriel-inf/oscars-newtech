package net.es.oscars.pce;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.resv.ent.Layer3FlowE;
import net.es.oscars.resv.ent.ScheduleSpecificationE;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Layer3PCE {


    public Layer3FlowE makeReserved(Layer3FlowE req_f, ScheduleSpecificationE scheduleSpec) throws PCEException {
        throw new PCEException("Layer 3 flows not supported yet");
    }



}
