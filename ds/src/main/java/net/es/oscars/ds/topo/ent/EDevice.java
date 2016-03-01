package net.es.oscars.ds.topo.ent;

import lombok.Data;
import lombok.NonNull;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Audited
public class EDevice {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    private String name;


    @OneToMany
    @NonNull
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    @Cascade(CascadeType.ALL)
    private Set<EIfce> ifces = new HashSet<>();

    public EDevice() {

    }


    public EDevice(String name) {
        this.name = name;
    }

}
