package net.es.oscars.webui.cont;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.spec.Blueprint;
import net.es.oscars.dto.spec.Specification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @RequestMapping("/resv_new")
    public String resv_new(Model model) {
        Specification specification = Specification.builder()
                .durationMinutes(0L)
                .notAfter(new Date())
                .notBefore(new Date())
                .requested(new Blueprint())
                .reserved(new Blueprint())
                .specificationId("")
                .description("")
                .version(0)
                .username("")
                .submitted(new Date())
                .build();

        model.addAttribute("specification", specification);


        return "resv_new";
    }



}