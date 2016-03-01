package net.es.oscars.ds.acct.ent;


import lombok.Data;
import lombok.NonNull;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.Collection;
import java.util.HashSet;

@Data
@Entity
@Audited
public class ECustomer {


    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    @Column(unique = true)
    private String name;

    @ElementCollection
    private Collection<String> projects = new HashSet<>();
    public ECustomer() {

    }

    public ECustomer(String name) {
        this.name = name;
    }

    public ECustomer(String name, Collection<String> projects) {
        this.name = name;
        this.projects = projects;
    }
}
