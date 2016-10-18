package net.es.oscars.topo.ent;

import lombok.*;

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.List;

@Data
@Embeddable
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EdgeE {

    private String origin;

    private String target;

}
