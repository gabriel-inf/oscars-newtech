package net.es.oscars.topo.pop;

import lombok.*;
import net.es.oscars.dto.topo.enums.Layer;
import net.es.oscars.topo.ent.IntRangeE;
import net.es.oscars.dto.topo.enums.DeviceModel;
import net.es.oscars.dto.topo.enums.DeviceType;


import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Device {


    @NonNull
    private String urn;

    private Set<Layer> capabilities = new HashSet<>();

    private DeviceModel model;
    private DeviceType type;

    @NonNull
    private Set<IntRangeE> reservableVlans;

    @NonNull
    private Set<Ifce> ifces = new HashSet<>();


}