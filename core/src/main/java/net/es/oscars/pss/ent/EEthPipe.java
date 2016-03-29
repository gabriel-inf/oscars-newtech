package net.es.oscars.pss.ent;

import lombok.*;
import net.es.oscars.pss.enums.EthPipeType;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EEthPipe {

    @Id
    @GeneratedValue
    private Long id;

    @OneToOne
    private EEthJunction a;

    @OneToOne
    private EEthJunction z;

    @OneToOne
    private EEthValve azValve;

    @NonNull
    private EthPipeType pipeType;

    @NonNull
    @ElementCollection
    private List<String> azPath;


}
