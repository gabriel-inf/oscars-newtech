package net.es.oscars.ds.pss.ent;

import lombok.Data;
import lombok.NonNull;

import javax.persistence.*;

@Data
@Entity
public class ETemplate {

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

    public ETemplate(String name) {
        this.name = name;
    }

    public ETemplate() {

    }
}
