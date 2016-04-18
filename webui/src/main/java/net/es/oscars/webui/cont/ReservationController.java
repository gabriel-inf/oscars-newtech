package net.es.oscars.webui.cont;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.dto.auth.User;
import net.es.oscars.dto.spec.Blueprint;
import net.es.oscars.dto.spec.Specification;
import net.es.oscars.webui.RestAuthProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.HashSet;

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
        Blueprint requested = Blueprint.builder().layer3Flows(new HashSet<>()).vlanFlows(new HashSet<>()).build();
        Blueprint reserved = Blueprint.builder().layer3Flows(new HashSet<>()).vlanFlows(new HashSet<>()).build();

        Specification specification = Specification.builder()
                .durationMinutes(0L)
                .notAfter(new Date())
                .notBefore(new Date())
                .requested(requested)
                .reserved(reserved)
                .specificationId("")
                .description("")
                .version(0)
                .username("")
                .submitted(new Date())
                .build();

        model.addAttribute("specification", specification);


        return "resv_new";
    }


    @RequestMapping(value="/resv_new_submit", method = RequestMethod.POST)
    public String resv_new_submit(@ModelAttribute Specification addedSpecification) {
        log.info("adding a spec ");

        String restPath = "https://localhost:8000/spec/add";
        Specification spec = restTemplate.postForObject(restPath, addedSpecification, Specification.class);

        log.info("added spec, id set to "+spec.getId());

        return "redirect:/resv_view/" + spec.getId();

    }

    @RequestMapping("/resv_view/{id}")
    public String resv_view(@PathVariable Long id, Model model) {
        String restPath = "https://localhost:8000/spec/get/" + id;

        Specification spec = restTemplate.getForObject(restPath, Specification.class);

        model.addAttribute("specification", spec.toString());
        return "admin_user_edit";


    }
}