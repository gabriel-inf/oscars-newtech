package net.es.oscars.webui.cont;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.resv.Connection;
import net.es.oscars.dto.spec.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import java.util.Date;

@Slf4j
@Controller
public class ReservationController {

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping("/resv_list")
    public String resv_list(Model model) {

        return "resv_list";
    }

    @RequestMapping("/resv_basic_new")
    public String resv_basic_new(Model model) {

        BasicVlanFlow flow = BasicVlanFlow.builder()
                .aDeviceUrn("")
                .aUrn("")
                .aVlanExpression("")
                .azMbps(0)
                .zDeviceUrn("")
                .zUrn("")
                .zVlanExpression("")
                .zaMbps(0)
                .build();

        ScheduleSpecification ss = ScheduleSpecification.builder()
                .durationMinutes(0L)
                .notBefore(new Date())
                .notAfter(new Date())
                .build();

        BasicVlanSpecification basicSpec = BasicVlanSpecification.builder()
                .connectionId("")
                .basicVlanFlow(flow)
                .scheduleSpec(ss)
                .specificationId(0L)
                .description("")
                .username("")
                .build();

        model.addAttribute("basicSpec", basicSpec);

        return "resv_basic_new";
    }


    @RequestMapping(value="/resv_basic_new_submit", method = RequestMethod.POST)
    public String resv_basic_new_submit(@ModelAttribute BasicVlanSpecification addedSpecification) {
        log.info("adding a basic vlan spec ");

        String restPath = "https://localhost:8000/resv/basic_vlan/add";
        Connection conn = restTemplate.postForObject(restPath, addedSpecification, Connection.class);


        return "redirect:/resv_view/" + conn.getConnectionId();

    }

    @RequestMapping("/resv_view/{connectionId}")
    public String resv_view(@PathVariable String connectionId, Model model) {
        String restPath = "https://localhost:8000/resv/get/" + connectionId;

        Connection conn = restTemplate.getForObject(restPath, Connection.class);
        ObjectMapper mapper = new ObjectMapper();

        String pretty = null;
        try {
            pretty = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(conn);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }


        model.addAttribute("connection", pretty);
        return "resv_view";


    }
}