package net.es.oscars.ds.topo.svc;

import lombok.extern.slf4j.Slf4j;
import net.es.oscars.ds.topo.dao.DevGroupRepository;
import net.es.oscars.ds.topo.ent.EDevGroup;
import org.hibernate.Session;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.history.Revision;
import org.springframework.data.history.Revisions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class TopoService {

    @Autowired
    private DevGroupRepository groupRepo;

    @Autowired
    private EntityManager entityManager;

    public EDevGroup save(EDevGroup group) {
        return groupRepo.save(group);
    }



    public String dumpAllRevisionsForDevGroupName(String name) {
        // needs to be in a transactional
        Session session = entityManager.unwrap(Session.class);

        AuditReader auditReader = AuditReaderFactory.get(session);
        AuditQuery auditQuery = auditReader.createQuery().forRevisionsOfEntity(EDevGroup.class, true, false);
        List<EDevGroup> resultList = auditQuery
                .add(AuditEntity.property("name").eq(name))
                .getResultList();

        List<String> dumps = resultList.stream().map(EDevGroup::toString).collect(Collectors.toList());

        return "dumpAll::: " + String.join(" ::: ", dumps);

    }

    public String dumpRevs(EDevGroup group) {

        List<String> revStrings = new ArrayList<>();
        Revisions<Integer, EDevGroup> netRevisions = groupRepo.findRevisions(group.getId());
        for (Revision<Integer, EDevGroup> netRev : netRevisions) {
            EDevGroup revGroup = netRev.getEntity();
            Integer rev = netRev.getRevisionNumber();
//            String deviceNames = String.join(" ", revGroup.getDevices().stream().map(EDevice::getName).collect(Collectors.toList()));
            revStrings.add("rev: " + rev + " grp: " + group.toString());
        }
        return "dumpRevs::: "+String.join(" ::: ", revStrings);

    }

}
