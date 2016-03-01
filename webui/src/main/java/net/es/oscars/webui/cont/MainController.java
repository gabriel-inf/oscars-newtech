package net.es.oscars.webui.cont;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Controller
public class MainController {

    @Autowired
    private RestTemplate restTemplate;


    @RequestMapping("/")
    public String home(Model model) {
        return "redirect:/resv_list";
    }

    @RequestMapping("/login")
    public String loginPage(Model model) {
        return "login";
    }


    @RequestMapping(value = "/info/institutions", method = RequestMethod.GET)
    @ResponseBody
    public List<String> institution_suggestions() {
        log.info("giving suggestions");
        String restPath = "https://localhost:8000/users/institutions";

        String[] all_insts = restTemplate.getForObject(restPath, String[].class);
        List<String> institutions = new ArrayList<>();
        institutions.addAll(Arrays.asList(all_insts));
        return institutions;
    }

}