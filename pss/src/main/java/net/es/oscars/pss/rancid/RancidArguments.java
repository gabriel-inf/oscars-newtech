package net.es.oscars.pss.rancid;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RancidArguments {
    private String executable;
    private String cloginrc;

    private String routerConfig;
    private String router;

}
