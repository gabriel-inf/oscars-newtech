package net.es.oscars.webui.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Filter {
    private Set<String> connectionIds;

    private Set<String> resvStates;

    private Set<String> operStates;

    private Set<String> provStates;

    private Set<String> userNames;

    private Set<Integer> bandwidths;

    private Set<String> startDates;

    private Set<String> endDates;
}
