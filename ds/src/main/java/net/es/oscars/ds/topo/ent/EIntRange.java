package net.es.oscars.ds.topo.ent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class EIntRange {
    private Integer floor;
    private Integer ceiling;
}
