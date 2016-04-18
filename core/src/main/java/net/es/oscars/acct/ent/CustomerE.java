package net.es.oscars.acct.ent;


import lombok.Data;
import lombok.NonNull;

import javax.persistence.*;
import java.util.Collection;
import java.util.HashSet;

@Data
@Entity
public class CustomerE {


    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    @Column(unique = true)
    private String name;

    @ElementCollection
    private Collection<String> projects = new HashSet<>();
    public CustomerE() {

    }

    public CustomerE(String name) {
        this.name = name;
    }

    public CustomerE(String name, Collection<String> projects) {
        this.name = name;
        this.projects = projects;
    }
}
