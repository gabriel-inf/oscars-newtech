package net.es.oscars.spec.ent;

import lombok.*;
import net.es.oscars.pss.enums.EthPipeType;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EVlanPipe {

    @Id
    @GeneratedValue
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    private EVlanJunction aJunction;

    @OneToOne(cascade = CascadeType.ALL)
    private EVlanJunction zJunction;

    @NonNull
    private Integer azMbps;


    @NonNull
    @ElementCollection
    private List<String> azERO;

    @NonNull
    private EthPipeType pipeType;


    @ElementCollection
    private Set<String> resourceIds;

}
