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

    @OneToOne
    private EJunction a;

    @OneToOne
    private EJunction z;

    @OneToOne
    private EValve azValve;

    @NonNull
    @ElementCollection
    private List<String> azPath;


}
