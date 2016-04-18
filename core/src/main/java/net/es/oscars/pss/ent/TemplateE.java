package net.es.oscars.pss.ent;

import lombok.*;

import javax.persistence.*;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateE {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    @Column(unique = true)
    private String name;

    @NonNull
    @Lob
    @Column(length = 65535)
    private String contents;

}
