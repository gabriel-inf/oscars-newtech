package net.es.oscars.acct;

import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class AcctPopulator {

    public void fill() {
        Map<String, String[]> initialCustomers = new HashMap<>();
        String[] defaultProjects = {"default project"};
        String[] alphaProjects = {"Alpha One", "Alpha Two"};
        String[] bravoProjects = {"Bravo One", "Bravo Five"};

        initialCustomers.put("default", defaultProjects);
        initialCustomers.put("alpha", alphaProjects);
        initialCustomers.put("bravo", bravoProjects);


    }


}
