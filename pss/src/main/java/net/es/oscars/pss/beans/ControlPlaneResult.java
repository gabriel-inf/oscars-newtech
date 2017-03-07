package net.es.oscars.pss.beans;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.es.oscars.dto.pss.st.ControlPlaneStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ControlPlaneResult {
    private String details;
    private ControlPlaneStatus status;
}
