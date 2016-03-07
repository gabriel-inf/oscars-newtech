package net.es.oscars.dto.resv;

import lombok.Data;
import net.es.oscars.common.resv.*;

import java.time.Instant;


@Data
public class ReservedResource<T> implements IReserved {
    public ReservedResource() {

    }

    private ResourceType resourceType;

    private T resource;

    private String urn;

    private Instant validFrom;

    private Instant validUntil;

}
