package net.es.oscars.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IntRange {
    private Integer floor;
    private Integer ceiling;
    public boolean contains(Integer i) {
        return (floor <= i && ceiling >= i);
    }
}
