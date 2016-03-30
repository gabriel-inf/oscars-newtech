package net.es.oscars.spec.ent;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EPipe {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    private String aJunctionId;

    @NonNull
    private String zJunctionId;

    @NonNull
    private String azValveId;

    @NonNull
    @ElementCollection
    private List<String> azPath;


}
