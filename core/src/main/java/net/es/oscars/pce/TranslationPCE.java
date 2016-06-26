package net.es.oscars.pce;


import lombok.extern.slf4j.Slf4j;
import net.es.oscars.pss.PCEAssistant;
import net.es.oscars.pss.PSSException;
import net.es.oscars.resv.dao.ReservedBandwidthRepository;
import net.es.oscars.resv.dao.ReservedPssResourceRepository;
import net.es.oscars.resv.dao.ReservedVlanRepository;
import net.es.oscars.resv.ent.*;
import net.es.oscars.topo.beans.TopoEdge;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.UrnE;
import net.es.oscars.topo.enums.DeviceModel;
import net.es.oscars.topo.enums.Layer;
import net.es.oscars.topo.svc.TopoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class TranslationPCE {
    @Autowired
    private PCEAssistant pceAssistant;

    @Autowired
    private TopoService topoService;

    @Autowired
    private BandwidthPCE bwPCE;

    @Autowired
    private ReservedBandwidthRepository bwRepo;

    @Autowired
    private ReservedVlanRepository vlanRepo;

    @Autowired
    private ReservedPssResourceRepository pssResourceRepo;

    @Autowired
    private UrnRepository urnRepository;

    public ReservedVlanFlowE makeReservedFlow(RequestedVlanFlowE req_f, ScheduleSpecificationE schedSpec,
                                              Map<RequestedVlanPipeE, Map<String, List<TopoEdge>>> eroMapsForFlow) throws PSSException, PCEException {
        // Handle Single junctions
        Set<ReservedVlanJunctionE> rsv_junctions = new HashSet<>();
        for (RequestedVlanJunctionE bpJunction : req_f.getJunctions()) {
            log.info("making a simple junction: "+bpJunction);
            ReservedVlanJunctionE schJunction = this.reserveSimpleJunction(bpJunction, schedSpec);
            rsv_junctions.add(schJunction);
        }

        // Handle Creating Reserved Pipes
        Set<ReservedEthPipeE> rsv_pipes = new HashSet<>();
        // For each Requested Pipe, there is a Map containing the az and za list of edges that will together
        // make up the reserved pipe(s). There can be multiple reserved pipes per requested pipe.
        for(RequestedVlanPipeE pipe : eroMapsForFlow.keySet()){
            Map<String, List<TopoEdge>> eroMap = eroMapsForFlow.get(pipe);
            List<TopoEdge> azEros = eroMap.get("az");
            List<TopoEdge> zaEros = eroMap.get("za");
            rsv_pipes.addAll(makeReservedPipes(pipe, azEros, zaEros, schedSpec));
        }

        return ReservedVlanFlowE.builder().junctions(rsv_junctions).pipes(rsv_pipes).build();
    }

    /**
     *
     * @throws PSSException
     * @throws PCEException
     */
    private List<ReservedEthPipeE> makeReservedPipes(RequestedVlanPipeE reqPipe, List<TopoEdge> azERO,
                                                     List<TopoEdge> zaERO, ScheduleSpecificationE sched)
            throws PSSException, PCEException {

        List<ReservedEthPipeE> pipes = new ArrayList<>();
        Map<String, DeviceModel> deviceModels = topoService.deviceModels();


        // now, decompose the path
        List<Map<Layer, List<TopoEdge>>> azSegments = PCEAssistant.decompose(azERO, deviceModels);
        List<Map<Layer, List<TopoEdge>>> zaSegments = PCEAssistant.decompose(zaERO, deviceModels);


        Map<String , UrnE> urnMap = new HashMap<>();

        urnRepository.findAll().stream().forEach(u -> {
            urnMap.put(u.getUrn(), u);

        });

        // for each segment:
        // if it is an Ethernet segment, make junctions, one per device
        // if it is an MPLS segment, make a pipe
        // all the while, make sure to merge in the current first and last junctions as needed

        for (int i = 0; i < segments.size(); i++) {
            Map<Layer, List<TopoEdge>> segment = segments.get(i);
            Optional<RequestedVlanJunctionE> mergeA = Optional.empty();
            Optional<RequestedVlanJunctionE> mergeZ = Optional.empty();
            if (i == 0) {
                mergeA = Optional.of(reqPipe.getAJunction());
            }
            if (i == segments.size() - 1) {
                mergeZ = Optional.of(reqPipe.getZJunction());
            }

            List<TopoEdge> edges;

            if (segment.size() != 1) {
                throw new PCEException("invalid segmentation");
            }
            if (segment.containsKey(Layer.ETHERNET)) {
                if (segment.get(Layer.ETHERNET).size() != 3) {
                    throw new PCEException("invalid segmentation");
                }

                edges = segment.get(Layer.ETHERNET);

                // an ethernet segment: a list of junctions, one per device

                // TODO: do something with these junctions!
                List<RequestedVlanJunctionE> vjs = pceAssistant.makeEthernetJunctions(edges,
                        reqPipe.getAzMbps(), reqPipe.getZaMbps(),
                        mergeA, mergeZ,
                        urnMap,
                        deviceModels);


            } else if (segment.containsKey(Layer.MPLS)) {
                edges = segment.get(Layer.MPLS);
                // TODO: do something with this pipe!
                RequestedVlanPipeE pipe = pceAssistant.makeVplsPipe(edges, reqPipe.getAzMbps(), reqPipe.getZaMbps(), mergeA, mergeZ, urnMap, deviceModels);
                pipes.add(pipe);
            } else {
                throw new PCEException("invalid segmentation");
            }
        }

        // TODO: decide VLANs and reserve them
        // TODO: collect needed resources from pceAssistant



    }


    private ReservedVlanJunctionE reserveSimpleJunction(RequestedVlanJunctionE req_j, ScheduleSpecificationE scheduleSpec) throws PCEException, PSSException {
        String deviceUrn = req_j.getDeviceUrn().getUrn();
        // TODO: check if present
        UrnE urn = urnRepository.findByUrn(deviceUrn).get();

        ReservedVlanJunctionE rsv_j = ReservedVlanJunctionE.builder()
                .deviceUrn(urn)
                .fixtures(new HashSet<>())
                .junctionType(pceAssistant.decideJunctionType(urn.getDeviceModel()))
                .build();


        // TODO: reserve fixture resources.


        return rsv_j;
    }


}
