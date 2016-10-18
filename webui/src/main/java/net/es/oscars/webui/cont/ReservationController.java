package net.es.oscars.webui.cont;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.pss.EthJunctionType;
import net.es.oscars.dto.resv.Connection;
import net.es.oscars.dto.resv.ConnectionFilter;
import net.es.oscars.dto.spec.*;
import net.es.oscars.dto.topo.Urn;
import net.es.oscars.webui.dto.MinimalRequest;
import net.es.oscars.webui.ipc.ConnectionProvider;
import net.es.oscars.webui.ipc.MinimalRequester;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Slf4j
@Controller
public class ReservationController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private MinimalRequester minimalRequester;

    @Autowired
    private ConnectionProvider connectionProvider;

    @RequestMapping("/resv/view/{connectionId}")
    public String resv_view(@PathVariable String connectionId, Model model) {
        String restPath = "https://localhost:8000/resv/get/" + connectionId;

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


        String restPath = "https://localhost:8000/resv/commit/" + connectionId;
        Connection c = restTemplate.getForObject(restPath, Connection.class);
        try {
            Thread.sleep(500L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

}