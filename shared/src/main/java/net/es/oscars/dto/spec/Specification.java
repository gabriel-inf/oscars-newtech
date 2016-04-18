package net.es.oscars.dto.spec;

import lombok.*;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Specification {
    private Long id;

    @NonNull
    private Integer version;

    @NonNull
    private String specificationId;

    @NonNull
    private Date submitted;

    @NonNull
    private Date notBefore;

    @NonNull
    private Date notAfter;

    @NonNull
    private Long durationMinutes;

    @NonNull
    private String description;

    @NonNull
    private String username;

    @NonNull
    private Blueprint requested;

    @NonNull
    private Blueprint reserved;

}
