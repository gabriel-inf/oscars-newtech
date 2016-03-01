package net.es.oscars.ds.authnz.ent;


import lombok.Data;
import lombok.NonNull;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
public class EUser implements Serializable {
    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    @Column(unique = true)
    private String username = "default";

    @NonNull
    @Column
    private String password = "";

    @Column
    private String certIssuer;

    @Column
    private String certSubject;

    @Column
    private String fullName;

    @Column
    private String email;

    @Column
    private String institution;

    @Embedded
    private EPermissions permissions = new EPermissions();

    public EUser() {

    }

    public EUser(String username, String password) {
        this.username = username;
        this.password = password;
    }

}
