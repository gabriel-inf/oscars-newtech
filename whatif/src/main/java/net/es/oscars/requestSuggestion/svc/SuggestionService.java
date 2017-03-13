package net.es.oscars.requestSuggestion.svc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.bwavail.BandwidthAvailabilityRequest;
import net.es.oscars.dto.bwavail.BandwidthAvailabilityResponse;
import net.es.oscars.dto.resv.Connection;
import net.es.oscars.dto.spec.Specification;
import net.es.oscars.oscarsapi.BandwidthController;
import net.es.oscars.oscarsapi.RequestController;
import net.es.oscars.requestSuggestion.dto.VolumeRequestSpecification;
import net.es.oscars.simplebwavail.svc.BandwidthAvailabilityGenerationService;
import net.es.oscars.simpleresv.svc.ConnectionGenerationService;
import net.es.oscars.simpleresv.svc.DateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class SuggestionService {

    @Autowired
    DateService dateService;

    @Autowired
    ConnectionGenerationService connectionGenerationService;

    @Autowired
    RequestController requestController;

    @Autowired
    BandwidthController bandwidthController;

    @Autowired
    BandwidthAvailabilityGenerationService bwAvailGenService;

    /**
     * Generate a list of suggested Connections given a VolumeRequestSpecification.
     * @param volumeSpec - Specification containing the source, destination, start date, end date, and data volume.
     * @return A list of viable connection options.
     */
    public List<Connection> generateSuggestions(VolumeRequestSpecification volumeSpec) {
        List<Connection> suggestions = new ArrayList<>();

        String startDate = volumeSpec.getStartDate();
        String endDate = volumeSpec.getEndDate();
        String srcDevice = volumeSpec.getSrcDevice();
        Set<String> srcPorts = volumeSpec.getSrcPorts();
        String dstDevice = volumeSpec.getDstDevice();
        Set<String> dstPorts = volumeSpec.getDstPorts();

        System.out.println("hello world!");

        // Get the bandwidth availability along a path from srcDevice to dstDevice
        BandwidthAvailabilityRequest bwAvailRequest = createBwAvailRequest(srcDevice, srcPorts, dstDevice, dstPorts,
                0, 0, startDate, endDate);
        BandwidthAvailabilityResponse bwResponse = bandwidthController.getBandwidthPath(bwAvailRequest);

        // TODO: Generate a number of values for azMbps and zaMbps.

        // Determine these values - Bandwidth from src -> dst (a -> z), and from dst -> src (z -> a)
        // Values may be the same, or different
        Integer azMbps = 0;
        Integer zaMbps = 0;
        // Each connection must have a unique id
        String connectionId = "test";

        // To determine if specific bandwidth values will work, you can create a connection object

        // Create an initial connection from parameters
        Connection initialConn = createInitialConnection(srcDevice, srcPorts, dstDevice, dstPorts, azMbps, zaMbps,
                connectionId, startDate, endDate);

        // Run a precheck
        Connection result = requestController.preCheck(initialConn);

        // Determine if result is successful. If so, consider storing it as an option
        if(result.getReserved().getVlanFlow().getAllPaths().size() > 0){
            // Success!
        }

        return suggestions;
    }

    private BandwidthAvailabilityRequest createBwAvailRequest(String srcDevice, Set<String> srcPorts, String dstDevice,
                                                              Set<String> dstPorts, Integer minAzMbps, Integer minZaMbps,
                                                              String startDate, String endDate) {
       return bwAvailGenService.generateBandwidthAvailabilityRequest(srcDevice, srcPorts, dstDevice, dstPorts,
               new ArrayList<>(), new ArrayList<>(), minAzMbps, minZaMbps, 1, true, startDate, endDate);
    }

    public Connection createInitialConnection(String srcDevice, Set<String> srcPorts, String dstDevice, Set<String> dstPorts,
                                              Integer azMbps, Integer zaMbps, String connectionId, String startDate,
                                              String endDate){
        Specification spec = connectionGenerationService.generateSpecification(srcDevice, srcPorts, dstDevice, dstPorts,
                "any", "any", azMbps, zaMbps, new ArrayList<>(), new ArrayList<>(), new HashSet<>(),
                "PALINDROME", "NONE", 1, 1, 1, 1, connectionId,
                startDate, endDate);
        return connectionGenerationService.buildConnection(spec);
    }
}
