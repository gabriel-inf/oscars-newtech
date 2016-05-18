package net.es.oscars.resv.ent;

import lombok.*;
import net.es.oscars.dto.pss.Layer3PipeType;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Layer3PipeE {

    @Id
    @GeneratedValue
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    private Layer3JunctionE aJunction;

    @OneToOne(cascade = CascadeType.ALL)
    private Layer3JunctionE zJunction;

    @NonNull
    private Integer azMbps;


    @NonNull
    @ElementCollection
    private List<String> azERO;

    @NonNull
    private Layer3PipeType pipeType;


    @ElementCollection
    private Set<String> resourceIds;

}
