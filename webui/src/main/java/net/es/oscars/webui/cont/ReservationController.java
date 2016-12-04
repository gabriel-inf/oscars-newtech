package net.es.oscars.webui.cont;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.bwavail.PortBandwidthAvailabilityRequest;
import net.es.oscars.dto.bwavail.PortBandwidthAvailabilityResponse;
import net.es.oscars.dto.resv.Connection;
import net.es.oscars.dto.resv.ConnectionFilter;
import net.es.oscars.dto.topo.BidirectionalPath;
import net.es.oscars.dto.topo.Edge;
import net.es.oscars.webui.dto.MinimalRequest;
import net.es.oscars.webui.ipc.ConnectionProvider;
import net.es.oscars.webui.ipc.MinimalPreChecker;
import net.es.oscars.webui.ipc.MinimalRequester;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Controller
public class ReservationController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private MinimalRequester minimalRequester;

    @Autowired
    private MinimalPreChecker minimalPreChecker;

    @Autowired
    private ConnectionProvider connectionProvider;

    private final String oscarsUrl = "https://localhost:8000";

    @RequestMapping("/resv/view/{connectionId}")
    public String resv_view(@PathVariable String connectionId, Model model) {
        String restPath = oscarsUrl + "/resv/get/" + connectionId;

        Connection conn = restTemplate.getForObject(restPath, Connection.class);
        ObjectMapper mapper = new ObjectMapper();
/*
        String pretty = null;
        try {
            pretty = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(conn);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        */
        model.addAttribute("connectionId", conn.getConnectionId());


        model.addAttribute("connection", conn);
        return "resv_view";
    }


    @RequestMapping("/resv/list")
    public String resv_list(Model model) {
        ConnectionFilter f = ConnectionFilter.builder().build();
        Set<Connection> connections = connectionProvider.filtered(f);

        model.addAttribute("connections", connections);

        return "resv_list";
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


    @RequestMapping("/resv/newConnectionId")
    @ResponseBody
    public Map<String, String> new_connection_id() {
        Map<String, String> result = new HashMap<>();
        result.put("connectionId", UUID.randomUUID().toString());
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
        Connection c = minimalRequester.holdMinimal(request);
        try {
            Thread.sleep(500L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Map<String, String> res = new HashMap<>();
        res.put("connectionId", c.getConnectionId());

        return res;

    }


    @RequestMapping(value = "/resv/precheck", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, String> resv_preCheck(@RequestBody MinimalRequest request)
    {
        Connection c = minimalPreChecker.preCheckMinimal(request);

        Map<String, String> res = new HashMap<>();

        res.put("connectionId", request.getConnectionId());

        //TODO: Pass back reservation with all details
        if(c == null)
        {
            res.put("preCheckResult", "UNSUCCESSFUL");
            log.info("Pre-Check Result: UNSUCCESSFUL");
        }
        else
        {
            res.put("preCheckResult", "SUCCESS");
            log.info("Pre-Check Result: SUCCESS");

            Set<BidirectionalPath> allPaths = c.getReserved().getVlanFlow().getAllPaths();

            String pathList = new String();

            for(BidirectionalPath biPath : allPaths)
            {
                List<Edge> oneAzPath = biPath.getAzPath();

                for(Edge oneEdge : oneAzPath)
                {
                    pathList += oneEdge.getOrigin() + "," + oneEdge.getTarget() + ",";
                }

                pathList += ";";
            }

            res.put("allAzPaths", pathList);
        }

        return res;
    }

    @RequestMapping(value = "/resv/topo/bwAvailAllPorts/", method = RequestMethod.POST)
    @ResponseBody
    public PortBandwidthAvailabilityResponse queryPortBwAvailability(@RequestBody MinimalRequest request)
    {
        log.info("Querying for Port Bandwdidth Availability");
        PortBandwidthAvailabilityRequest bwRequest = new PortBandwidthAvailabilityRequest();
        Date startDate = new Date(request.getStartAt() * 1000L);
        Date endDate = new Date(request.getEndAt() * 1000L);

        bwRequest.setStartDate(startDate);
        bwRequest.setEndDate(endDate);

        String submitUrl = "/bwavail/ports";
        String restPath = oscarsUrl  + submitUrl;

        PortBandwidthAvailabilityResponse bwResponse = restTemplate.postForObject(restPath, bwRequest, PortBandwidthAvailabilityResponse.class);

        return bwResponse;
    }


    @RequestMapping("/resv/timebw")
    public String resv_timebar(Model model) {
        return "timeBw";
    }
}