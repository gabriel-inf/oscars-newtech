package net.es.oscars.webui.cont;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.bwavail.PortBandwidthAvailabilityRequest;
import net.es.oscars.dto.bwavail.PortBandwidthAvailabilityResponse;
import net.es.oscars.dto.resv.Connection;
import net.es.oscars.dto.resv.ConnectionFilter;
import net.es.oscars.dto.resv.precheck.PreCheckResponse;
import net.es.oscars.dto.spec.RequestedVlanPipe;
import net.es.oscars.dto.topo.BidirectionalPath;
import net.es.oscars.dto.topo.Edge;
import net.es.oscars.webui.dto.AdvancedRequest;
import net.es.oscars.webui.dto.MinimalRequest;
import net.es.oscars.webui.ipc.ConnectionProvider;
import net.es.oscars.webui.ipc.PreChecker;
import net.es.oscars.webui.ipc.Requester;
import org.apache.commons.lang3.StringUtils;
import org.hashids.Hashids;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@Slf4j
@Controller
public class ReservationController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private Requester requester;

    @Autowired
    private PreChecker preChecker;

    @Autowired
    private ConnectionProvider connectionProvider;

    private final String oscarsUrl = "https://localhost:8000";


    @ResponseBody
    @ExceptionHandler(RestClientException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleRestError(RestClientException ex) {
        log.error(ex.getMessage());
        HashMap<String, Object> result = new HashMap<>();
        result.put("error", true);
        result.put("error_message", ex.getMessage());
        return result;
    }


    @RequestMapping("/resv/view/{connectionId}")
    public String resv_view(@PathVariable String connectionId, Model model) {

        Connection conn = requester.getConnection(connectionId);
        model.addAttribute("connectionId", conn.getConnectionId());


        model.addAttribute("connection", conn);
        return "resv_view";
    }

    @RequestMapping(value = "/resv/get/{connectionId}", method = RequestMethod.GET)
    @ResponseBody
    public Connection resv_get_details(@PathVariable String connectionId) {
        return requester.getConnection(connectionId);
    }


    @RequestMapping("/resv/list")
    public String resv_list(Model model) {
        return "resv_list";
    }


    @RequestMapping(value = "/resv/list/allconnections", method = RequestMethod.GET)
    @ResponseBody
    public Set<Connection> resv_list_connections() {
        ConnectionFilter f = ConnectionFilter.builder().build();
        Set<Connection> filteredConnections = connectionProvider.filtered(f);

        for (Connection c : filteredConnections) {
            Set<RequestedVlanPipe> pipes = c.getSpecification().getRequested().getVlanFlow().getPipes();
        }

        return filteredConnections;
    }


    @RequestMapping(value = "/resv/commit/{connectionId}", method = RequestMethod.GET)
    public String connection_commit(@PathVariable String connectionId, Model model) {


        try {
            Thread.sleep(500L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String restPath = oscarsUrl + "/resv/commit/" + connectionId;
        Connection c = restTemplate.getForObject(restPath, Connection.class);
        return "redirect:/resv/view/" + c.getConnectionId();

    }

    @RequestMapping(value = "/resv/commit", method = RequestMethod.POST)
    @ResponseBody
    public String connection_commit_react(@RequestBody String connectionId) {

        try {
            Thread.sleep(500L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String restPath = oscarsUrl + "/resv/commit/" + connectionId;
        Connection c = restTemplate.getForObject(restPath, Connection.class);
        return c.getConnectionId();

    }


    @RequestMapping(value = "/resv/newConnectionId", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> new_connection_id() {
        Map<String, String> result = new HashMap<>();

        Hashids hashids = new Hashids("oscars");

        boolean found = false;
        Random rand = new Random();
        String connectionId = "";
        while (!found) {
            Integer id = rand.nextInt();
            if (id < 0) {
                id = -1 * id;
            }
            connectionId = hashids.encode(id);
            if (!requester.connectionIdExists(connectionId)) {
                // it's good that it doesn't exist, means we can use it
                found = true;
            }
        }

        result.put("connectionId", connectionId);
        log.info("provided new connection id: " + result.get("connectionId"));
        return result;
    }


    @RequestMapping("/resv/gui")
    public String resv_gui(Model model) {
        return "resv_gui";
    }


    @RequestMapping(value = "/resv/commands/{connectionId}/{deviceUrn}", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, String> commands(@PathVariable("connectionId") String connectionId,
                                        @PathVariable("deviceUrn") String deviceUrn) {
        log.info("getting commands for " + connectionId + " " + deviceUrn);
        String restPath = oscarsUrl + "/pss/commands/" + connectionId + "/" + deviceUrn;
        log.info("rest :" + restPath);

        Map<String, String> commands = restTemplate.getForObject(restPath, Map.class);

        return commands;
    }


    @RequestMapping(value = "/resv/minimal_hold", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, String> resv_minimal_hold(@RequestBody MinimalRequest request) {
        Connection c = requester.holdMinimal(request);
        try {
            Thread.sleep(500L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Map<String, String> res = new HashMap<>();
        res.put("connectionId", c.getConnectionId());

        return res;
    }

    @RequestMapping(value = "/resv/advanced_hold", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, String> resv_advanced_hold(@RequestBody AdvancedRequest request) {
        Connection c = requester.holdAdvanced(request);
        Map<String, String> res = new HashMap<>();
        res.put("connectionId", c.getConnectionId());

        return res;
    }


    @RequestMapping(value = "/resv/precheck", method = RequestMethod.POST)
    @ResponseBody
    public PreCheckResponse resv_preCheck(@RequestBody MinimalRequest request) {
        Connection c = preChecker.preCheckMinimal(request);
        log.info("Request Details: " + request.toString());

        return processPrecheckResponse(request.getConnectionId(), c);
    }

    @RequestMapping(value = "/resv/advanced_precheck", method = RequestMethod.POST)
    @ResponseBody
    public PreCheckResponse resv_precheck_advanced(@RequestBody AdvancedRequest request) {
        Connection c = preChecker.preCheckAdvanced(request);
        log.info("Request Details: " + request.toString());

        return processPrecheckResponse(request.getConnectionId(), c);
    }

    private PreCheckResponse processPrecheckResponse(String connectionId, Connection c) {
        PreCheckResponse response = PreCheckResponse.builder()
                .connectionId(connectionId)
                .linksToHighlight(new ArrayList<>())
                .nodesToHighlight(new ArrayList<>())
                .precheckResult(PreCheckResponse.PrecheckResult.SUCCESS)
                .build();

        //TODO: Pass back reservation with all details
        if (c == null) {
            response.setPrecheckResult(PreCheckResponse.PrecheckResult.UNSUCCESSFUL);
            log.info("Pre-Check Result: UNSUCCESSFUL");
        } else {
            log.info("Pre-Check Result: SUCCESS");

            Set<BidirectionalPath> allPaths = c.getReserved().getVlanFlow().getAllPaths();

            for (BidirectionalPath biPath : allPaths) {
                List<Edge> oneAzPath = biPath.getAzPath();

                // path always goes:
                // port -> device (always first)
                // device -> port
                // port -> port
                // ...
                //
                // port -> port
                // port -> device
                // device -> port
                // index 0 mod 3: origin is port, target is device
                // index 1 mod 3: origin is device , target is port
                // index 2 mod 3: origin is port, target is port

                Integer idx = 0;
                Set<String> nodesToHighlight = new HashSet<>();
                Set<String> linksToHighlight = new HashSet<>();

                for (Edge edge : oneAzPath) {
                    if (idx % 3 == 0) {
                        nodesToHighlight.add(edge.getTarget());
                        log.info("highlight device " + edge.getTarget());
                    } else if (idx % 3 == 2) {
                        String linkName = edge.getOrigin() + " -- "+edge.getTarget();
                        linksToHighlight.add(linkName);
                        log.info("highlight link " + linkName);
                    } else {
                        log.info("highlight device " + edge.getOrigin());
                        nodesToHighlight.add(edge.getOrigin());

                    }
                    idx++;
                }
                response.getNodesToHighlight().addAll(nodesToHighlight);
                response.getLinksToHighlight().addAll(linksToHighlight);
            }

        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            String pretty = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
            log.info(pretty);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return response;
    }

    @RequestMapping(value = "/resv/topo/bwAvailAllPorts/", method = RequestMethod.POST)
    @ResponseBody
    public PortBandwidthAvailabilityResponse queryPortBwAvailability(@RequestBody MinimalRequest request) {
        log.info("Querying for Port Bandwdidth Availability");
        PortBandwidthAvailabilityRequest bwRequest = new PortBandwidthAvailabilityRequest();
        Date startDate = new Date(request.getStartAt() * 1000L);
        Date endDate = new Date(request.getEndAt() * 1000L);

        bwRequest.setStartDate(startDate);
        bwRequest.setEndDate(endDate);

        String submitUrl = "/bwavail/ports";
        String restPath = oscarsUrl + submitUrl;

        PortBandwidthAvailabilityResponse bwResponse = restTemplate.postForObject(restPath, bwRequest, PortBandwidthAvailabilityResponse.class);

        return bwResponse;
    }


    @RequestMapping("/resv/timebw")
    public String resv_timebar(Model model) {
        return "timeBw";
    }
}