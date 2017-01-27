
Top PCE Module
==============

The Top PCE represents the PCE's API through which circuits are submitted for request, and are returned updated upon successful (or unsuccessful) reservation. Additionally, this module serves as the OSCARS Reservation Workflow Control mechanism which decomposes a :doc:`../requestspec_doc` into its :doc:`../plumbing_doc` containers and determines how to process each pipe. Note that these pipes are specified independently, meaning that the Top PCE may prescribe different flows through the remainder of the PCE for a single circuit request. The Top PCE feeds each pipe through one or more service-specific PCE submodules in order to perform the desired constrained pathfinding. The desired services are triggered during request specification, and the Top PCE will determine which of the appropriate PCE submodules to engage for supporting them. 

Module Details
--------------
**Calls:**

- :doc:`pce_ero_doc`
- :doc:`pce_layer3_doc`
- :doc:`pce_nonpalindrome_doc`
- :doc:`pce_palindrome_doc`
- :doc:`pce_survivability_doc`
- :doc:`pce_translation_doc`
- :doc:`../service/service_topology_doc`
- :doc:`../service/service_pruning_doc`
- :doc:`../service/service_bandwidth_doc`
- :doc:`../service/service_vlan_doc`

**Called By:** 

- :doc:`../service/service_bandwidth_availability_doc`
- :doc:`../service/service_resv_doc`

**API Specification:**

- :java:ref:`TopPCE`


