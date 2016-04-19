package net.es.oscars.resv.ent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.es.oscars.st.oper.OperState;
import net.es.oscars.st.prov.ProvState;
import net.es.oscars.st.resv.ResvState;

import javax.persistence.Embeddable;

@Data
@Embeddable
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatesE {

    private ResvState resv;

    private ProvState prov;

    private OperState oper;
}
