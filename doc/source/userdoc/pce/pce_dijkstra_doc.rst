
Dijkstra PCE Module
===================

This module is one of the fundamental pathfinding elements of the PCE, used to compute shortest (least-cost) path routing on a constrained network. Dijsktra's Algorithm_ is one of the most widely adopted routing algorithms because it optimally computes its path. Most of the service-specific PCE modules rely on the Dijkstra PCE for the low-level path computation after constraining the topology for their specified services.

.. _Algorithm: https://en.wikipedia.org/wiki/Dijkstra's_algorithm

Module Details
--------------
**Calls:**

- :doc:`../service/service_topology_doc`

**Called By:** 

- :doc:`pce_ero_doc`
- :doc:`pce_nonpalindrome_doc`
- :doc:`pce_palindrome_doc`
- :doc:`pce_survivability_doc`
- :doc:`../service/service_bandwidth_availability_doc`
- :doc:`..virtual_topo_doc`

**API Specification:**

- :java:ref:`DijkstraPCE`
