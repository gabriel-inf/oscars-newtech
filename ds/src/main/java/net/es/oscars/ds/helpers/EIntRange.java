package net.es.oscars.ds.helpers;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.es.oscars.common.IntRange;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class EIntRange {
    private Integer floor;
    private Integer ceiling;

    public IntRange toDtoIntRange() {
        return IntRange.builder().ceiling(ceiling).floor(floor).build();
    }
}
