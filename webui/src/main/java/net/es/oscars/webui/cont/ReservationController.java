package net.es.oscars.webui.cont;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Controller
public class ReservationController {

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping("/resv_list")
    public String resv_list(Model model) {

        return "resv_list";
    }




}