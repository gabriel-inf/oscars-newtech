package net.es.oscars.helpers;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.topo.Edge;
import net.es.oscars.dto.topo.enums.UrnType;
import net.es.oscars.resv.ent.*;
import net.es.oscars.topo.dao.UrnRepository;
import net.es.oscars.topo.ent.EdgeE;
import net.es.oscars.topo.ent.UrnE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ReservedEntityDecomposer {

    @Autowired
    private UrnRepository urnRepo;

    /*
        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        Decomposing a Reserved Blueprint
        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    */

    /**
     * Convert a reserved blueprint into a set of URNs. Retrieves URNs from repository.
     *
     * @param resBlueprint - The reserved blueprint
     * @return Set of all URNs that make up the reserved pipes/junction/fixtures in the blueprint.
     */
    public Set<UrnE> decomposeReservedBlueprint(ReservedBlueprintE resBlueprint) {
        Map<String, UrnE> urnMap = buildUrnMap();
        ReservedVlanFlowE flow = resBlueprint.getVlanFlow();
        return decomposeFlowElements(flow.getEthPipes(), flow.getMplsPipes(), flow.getJunctions(), urnMap);
    }

    /**
     * Convert a reserved blueprint into a set of URNs. Retrieves URNs from input list of URNs
     *
     * @param resBlueprint - The reserved blueprint
     * @param urns         - A list of URNs
     * @return Set of all URNs that make up the reserved pipes/junction/fixtures in the blueprint.
     */
    public Set<UrnE> decomposeReservedBlueprint(ReservedBlueprintE resBlueprint, List<UrnE> urns) {
        Map<String, UrnE> urnMap = buildUrnMap(urns);
        ReservedVlanFlowE flow = resBlueprint.getVlanFlow();
        return decomposeFlowElements(flow.getEthPipes(), flow.getMplsPipes(), flow.getJunctions(), urnMap);
    }

    /**
     * Convert a reserved blueprint into a set of URNs. Retrieves URNs from input URN map
     *
     * @param resBlueprint - The reserved blueprint
     * @param urnMap       - A map of URN strings to URN objects
     * @return Set of all URNs that make up the reserved pipes/junction/fixtures in the blueprint.
     */
    public Set<UrnE> decomposeReservedBlueprint(ReservedBlueprintE resBlueprint, Map<String, UrnE> urnMap) {
        ReservedVlanFlowE flow = resBlueprint.getVlanFlow();
        return decomposeFlowElements(flow.getEthPipes(), flow.getMplsPipes(), flow.getJunctions(), urnMap);
    }

    /*
        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        Decomposing Reserved ETHERNET Pipe
        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    */

    /**
     * Convert a reserved Ethernet pipe into a set of URNs. Retrieves URNs from URN repository.
     *
     * @param pipe - A reserved Ethernet pipe
     * @return Set of all URNs that make up the reserved junctions, fixtures, AND az/za EROs in the pipe
     */
    public Set<UrnE> decomposeEthPipe(ReservedEthPipeE pipe) {
        Map<String, UrnE> urnMap = buildUrnMap();
        return decomposePipeElements(pipe.getAJunction(), pipe.getZJunction(), pipe.getAzERO(), pipe.getZaERO(), urnMap);
    }

    /**
     * Convert a reserved Ethernet pipe into a set of URNs. Retrieves URNs from input URN list.
     *
     * @param pipe - A reserved Ethernet pipe
     * @param urns - A list of URN objects
     * @return Set of all URNs that make up the reserved junctions, fixtures, AND az/za EROs in the pipe
     */
    public Set<UrnE> decomposeEthPipe(ReservedEthPipeE pipe, List<UrnE> urns) {
        Map<String, UrnE> urnMap = buildUrnMap(urns);
        return decomposePipeElements(pipe.getAJunction(), pipe.getZJunction(), pipe.getAzERO(), pipe.getZaERO(), urnMap);
    }

    /**
     * Convert a reserved Ethernet pipe into a set of URNs. Retrieves URNs from input URN map.
     *
     * @param pipe   - A reserved Ethernet pipe
     * @param urnMap - A map of URN strings to URN objects
     * @return Set of all URNs that make up the reserved junctions, fixtures, AND az/za EROs in the pipe
     */
    public Set<UrnE> decomposeEthPipe(ReservedEthPipeE pipe, Map<String, UrnE> urnMap) {
        return decomposePipeElements(pipe.getAJunction(), pipe.getZJunction(), pipe.getAzERO(), pipe.getZaERO(), urnMap);
    }

    // Only retrieve the EROs

    public List<UrnE> decomposeEthPipeIntoAzEROList(ReservedEthPipeE pipe) {
        List<UrnE> urns = urnRepo.findAll();
        return decomposeEthPipeIntoAzEROList(pipe, urns);
    }

    public List<UrnE> decomposeEthPipeIntoZaEROList(ReservedEthPipeE pipe) {
        List<UrnE> urns = urnRepo.findAll();
        return decomposeEthPipeIntoZaEROList(pipe, urns);
    }

    public List<UrnE> decomposeEthPipeIntoAzEROList(ReservedEthPipeE pipe, List<UrnE> urns) {
        Map<String, UrnE> urnMap = buildUrnMap(urns);
        return decomposeEthPipeIntoAzEROList(pipe, urnMap);
    }

    public List<UrnE> decomposeEthPipeIntoZaEROList(ReservedEthPipeE pipe, List<UrnE> urns) {
        Map<String, UrnE> urnMap = buildUrnMap(urns);
        return decomposeEthPipeIntoZaEROList(pipe, urnMap);
    }

    public List<UrnE> decomposeEthPipeIntoAzEROList(ReservedEthPipeE pipe, Map<String, UrnE> urnMap) {
        return pipe.getAzERO().stream().filter(urnMap::containsKey).map(urnMap::get).collect(Collectors.toList());
    }

    public List<UrnE> decomposeEthPipeIntoZaEROList(ReservedEthPipeE pipe, Map<String, UrnE> urnMap) {
        return pipe.getZaERO().stream().filter(urnMap::containsKey).map(urnMap::get).collect(Collectors.toList());
    }

    /*
        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        Decomposing Reserved MPLS Pipe
        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    */

    /**
     * Convert a reserved MPLS pipe into a set of URNs. Retrieves URNs from URN repository.
     *
     * @param pipe - A MPLS Ethernet pipe
     * @return Set of all URNs that make up the reserved junctions, fixtures, AND az/za EROs in the pipe
     */
    public Set<UrnE> decomposeMplsPipe(ReservedMplsPipeE pipe) {
        Map<String, UrnE> urnMap = buildUrnMap();
        return decomposePipeElements(pipe.getAJunction(), pipe.getZJunction(), pipe.getAzERO(), pipe.getZaERO(), urnMap);
    }

    /**
     * Convert a reserved MPLS pipe into a set of URNs. Retrieves URNs from input URN list.
     *
     * @param pipe - A reserved MPLS pipe
     * @param urns - A list of URN objects
     * @return Set of all URNs that make up the reserved junctions, fixtures, AND az/za EROs in the pipe
     */
    public Set<UrnE> decomposeMplsPipe(ReservedMplsPipeE pipe, List<UrnE> urns) {
        Map<String, UrnE> urnMap = buildUrnMap(urns);
        return decomposePipeElements(pipe.getAJunction(), pipe.getZJunction(), pipe.getAzERO(), pipe.getZaERO(), urnMap);
    }

    /**
     * Convert a reserved MPLS pipe into a set of URNs. Retrieves URNs from input URN map.
     *
     * @param pipe   - A reserved MPLS pipe
     * @param urnMap - A map of URN strings to URN objects
     * @return Set of all URNs that make up the reserved junctions, fixtures, AND az/za EROs in the pipe
     */
    public Set<UrnE> decomposeMplsPipe(ReservedMplsPipeE pipe, Map<String, UrnE> urnMap) {
        return decomposePipeElements(pipe.getAJunction(), pipe.getZJunction(), pipe.getAzERO(), pipe.getZaERO(), urnMap);
    }

    // Only retrieve the EROs
    public List<UrnE> decomposeMplsPipeIntoAzEROList(ReservedMplsPipeE pipe) {
        List<UrnE> urns = urnRepo.findAll();
        return decomposeMplsPipeIntoAzEROList(pipe, urns);
    }

    public List<UrnE> decomposeMplsPipeIntoZaEROList(ReservedMplsPipeE pipe) {
        List<UrnE> urns = urnRepo.findAll();
        return decomposeMplsPipeIntoZaEROList(pipe, urns);
    }

    public List<UrnE> decomposeMplsPipeIntoAzEROList(ReservedMplsPipeE pipe, List<UrnE> urns) {
        Map<String, UrnE> urnMap = buildUrnMap(urns);
        return decomposeMplsPipeIntoAzEROList(pipe, urnMap);
    }

    public List<UrnE> decomposeMplsPipeIntoZaEROList(ReservedMplsPipeE pipe, List<UrnE> urns) {
        Map<String, UrnE> urnMap = buildUrnMap(urns);
        return decomposeMplsPipeIntoZaEROList(pipe, urnMap);
    }

    public List<UrnE> decomposeMplsPipeIntoAzEROList(ReservedMplsPipeE pipe, Map<String, UrnE> urnMap) {
        return pipe.getAzERO().stream().filter(urnMap::containsKey).map(urnMap::get).collect(Collectors.toList());
    }

    public List<UrnE> decomposeMplsPipeIntoZaEROList(ReservedMplsPipeE pipe, Map<String, UrnE> urnMap) {
        return pipe.getZaERO().stream().filter(urnMap::containsKey).map(urnMap::get).collect(Collectors.toList());
    }

    /*
        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        Decomposing Reserved Junction
        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    */

    /**
     * Convert a requested junction into a set of URNs. Retrieves URNs from junction and fixtures, no passed in URNs needed.
     *
     * @param junction - The reserved junction
     * @return Set of all URNs making up the junction (device) and fixtures (ports)
     */
    public Set<UrnE> decomposeJunction(ReservedVlanJunctionE junction) throws NoSuchElementException {
        Set<UrnE> urns = new HashSet<>();
        UrnE deviceUrnE = urnRepo.findByUrn(junction.getDeviceUrn()).get();
        urns.add(deviceUrnE);
        urns.addAll(junction.getFixtures().stream()
                .map(f -> urnRepo.findByUrn(f.getIfceUrn()).get())
                .collect(Collectors.toSet()));
        return urns;
    }

    /*
        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        Decompose Elements from a Flow
        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    */

    /**
     * Taking the key elements from a Flow, convert the Ethernet and MPLS pipes, along with all single junctions,
     * into one big set of URNs.
     *
     * @param ethPipes  - The reserved Ethernet pipes
     * @param mplsPipes - The reserved MPLS pipes
     * @param junctions - The reserved junctions
     * @param urnMap    - The URN map
     * @return Set of all URNs across all pipes/junctions.
     */
    private Set<UrnE> decomposeFlowElements(Set<ReservedEthPipeE> ethPipes, Set<ReservedMplsPipeE> mplsPipes,
                                            Set<ReservedVlanJunctionE> junctions, Map<String, UrnE> urnMap) {
        Set<UrnE> urns = new HashSet<>();
        urns.addAll(ethPipes.stream().map(pipe -> decomposeEthPipe(pipe, urnMap)).flatMap(Collection::stream).collect(Collectors.toSet()));
        urns.addAll(mplsPipes.stream().map(pipe -> decomposeMplsPipe(pipe, urnMap)).flatMap(Collection::stream).collect(Collectors.toSet()));
        urns.addAll(junctions.stream().map(this::decomposeJunction).flatMap(Collection::stream).collect(Collectors.toSet()));
        return urns;
    }

    /*
        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        Decompose Elements from a Pipe
        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    */

    /**
     * Take in the key elements from a pipe, convert them into set of URNs.
     *
     * @param aJunction - The "A" junction of the pipe
     * @param zJunction - The "Z" junction of the pipe
     * @param azERO     - List of strings representing the AZ path taken by the pipe (excluding junctions)
     * @param zaERO     - List of strings representing the ZA path taken by the pipe (excluding junctions)
     * @param urnMap    - Map of URN strings to Urn Entities
     * @return Union of all URNs used across the reserved pipe.
     */
    private Set<UrnE> decomposePipeElements(ReservedVlanJunctionE aJunction, ReservedVlanJunctionE zJunction,
                                            List<String> azERO, List<String> zaERO, Map<String, UrnE> urnMap) {
        Set<UrnE> urns = new HashSet<>();
        urns.addAll(decomposeJunction(aJunction));
        urns.addAll(decomposeJunction(zJunction));
        urns.addAll(azERO.stream().filter(urnMap::containsKey).map(urnMap::get).collect(Collectors.toSet()));
        urns.addAll(zaERO.stream().filter(urnMap::containsKey).map(urnMap::get).collect(Collectors.toSet()));
        return urns;
    }

    /*
        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        Building a URN map
        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    */

    /**
     * Retrive URNs from the URN repository, build a map.
     *
     * @return Map of URN string to URN entity
     */
    private Map<String, UrnE> buildUrnMap() {
        List<UrnE> allUrns = urnRepo.findAll();
        return allUrns.stream().collect(Collectors.toMap(UrnE::getUrn, u -> u));
    }

    /**
     * Given a list of URN entities, build a map.
     *
     * @param urns - List of URNs
     * @return Map of URN string to URN entity
     */
    private Map<String, UrnE> buildUrnMap(List<UrnE> urns) {
        return urns.stream().collect(Collectors.toMap(UrnE::getUrn, u -> u));
    }


    /*
        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        Decomposing an Edge List into a List<String>
        ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

     */

    public List<String> decomposeEdgeList(List<EdgeE> edges){
        // ASSUMPTION: Edge list comes in the order of [(Fix, Device), (Fix, Device)..., (Device, Port), (Port, Device), ...)]
        List<String> elements = new ArrayList<>();

        // Find first and last device
        String firstDeviceName = "";
        String lastDeviceName = "";
        for(EdgeE edge : edges){
            Optional<UrnE> targetOpt = urnRepo.findByUrn(edge.getTarget());
            if(targetOpt.isPresent() && targetOpt.get().getUrnType().equals(UrnType.DEVICE)){
                if(firstDeviceName.equals("")){
                    firstDeviceName = targetOpt.get().getUrn();
                }
                lastDeviceName = targetOpt.get().getUrn();
            }
        }

        // With first and last device names, track when firstDevice becomes ORIGIN of an edge, and lastDevice becomes TARGET
        Boolean destAdded = false;
        for(EdgeE edge : edges){
            if(edge.getOrigin().equals(lastDeviceName)){
                if(!destAdded) {
                    elements.add(edge.getOrigin());
                    elements.add(edge.getTarget());
                    destAdded = true;
                }
                else{
                    elements.add(edge.getTarget());
                }
            } else{
                elements.add(edge.getOrigin());
            }
        }

        return elements;
    }

    public List<UrnE> translateStringListToUrns(List<String> pathElements){
        List<UrnE> urns = new ArrayList<>();
        for(String pathElement : pathElements){
            Optional<UrnE> urnOpt = urnRepo.findByUrn(pathElement);
            if(urnOpt.isPresent()){
                urns.add(urnOpt.get());
            }
        }
        return urns;
    }
}
