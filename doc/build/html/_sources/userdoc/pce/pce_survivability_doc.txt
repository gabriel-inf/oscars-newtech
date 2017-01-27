
Survivability PCE Module
========================

This module finds a survivable pair or set of physically (node, port, and link) disjoint routes through the network for a given requested :doc:`../plumbing_doc` pipe. In the case where a pair of paths is required, the Survivability PCE relies upon the :doc:`pce_bhandari_doc`, which performs Bhandari's algorithm to minimize the total cost of both the primary working path and the secondary backup path. This differs from some heuristics which aim to minimize the cost of the primary path at the possible expense of a more costly secondary path.  Thus, both paths are computed simultaneously.  If an appropriate pair cannot be identified which meets the constraints in requested specification, the entire circuit reservation fails. If the user requests a set of *K* disjoint paths, all *K* must be computed or the reservation fails. 

This module provides the primary implementation for the :ref:`surv_pce_services`:

- :ref:`surv_pce_complete`
- :ref:`surv_pce_mpls`
- :ref:`surv_pce_kpath`

.. warning::

   As of February 1, 2017, K-path survivability is implemented only in the BhandariPCE, and there is not yet any avenue in the Survivability PCE API to support the reservation of K-paths at this time. This connective tissue will be incorporated into a future release version.


Module Details
--------------
**Calls:**

- :doc:`pce_bhandari_doc`
- :doc:`pce_dijkstra_doc`
- :doc:`../service/service_topology_doc`
- :doc:`../service/service_pruning_doc`
- :doc:`../virtual_topo_surv_doc`

**Called By:** 

- :doc:`pce_top_doc`

**API Specification:**

- :java:ref:`SurvivabilityPCE`

