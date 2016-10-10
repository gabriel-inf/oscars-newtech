package net.es.oscars.topo.ent;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EdgeE {
    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    String a;

    @NonNull
    String z;

}
