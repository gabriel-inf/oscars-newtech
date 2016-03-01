package net.es.oscars.ds.acct.pop;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.ds.acct.ent.ECustomer;
import net.es.oscars.ds.acct.svc.CustService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

@Slf4j
@Component
public class AcctPopulator {

    @Autowired
    private CustService service;

    @PostConstruct
    public void fill() {
        Map<String, String[]> initialCustomers = new HashMap<>();
        String[] defaultProjects = {"default project"};
        String[] alphaProjects = {"Alpha One", "Alpha Two"};
        String[] bravoProjects = {"Bravo One", "Bravo Five"};

        initialCustomers.put("default", defaultProjects);
        initialCustomers.put("alpha", alphaProjects);
        initialCustomers.put("bravo", bravoProjects);

        List<ECustomer> customers = service.findAll();

        if (customers.isEmpty()) {
            for (String custName : initialCustomers.keySet()) {
                Set<String> projects = new HashSet<>(Arrays.asList(initialCustomers.get(custName)));
                ECustomer defCustomer = new ECustomer(custName, projects);
                service.save(defCustomer);
                log.info("added default customer "+ defCustomer.toString());

            }
        } else {
            log.info("db not empty");

        }
    }





}
