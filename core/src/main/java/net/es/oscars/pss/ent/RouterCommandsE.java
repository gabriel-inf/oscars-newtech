package net.es.oscars.pss.ent;

import lombok.*;

import javax.persistence.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouterCommandsE {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    private String deviceUrn;

    @NonNull
    private String connectionId;

    @NonNull
    @Lob
    @Column(length = 65535)
    private String contents;

}
