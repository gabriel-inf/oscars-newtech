.. _surv_pce_services:

Survivable Routing Services
===========================



.. _surv_pce_complete:

Complete Path Survivability
---------------------------

Figure 15: End-to-End (excluding the device -> fixture links) link-disjoint Primary and Backup paths.

Network elements, particularly links, experience failures almost regularly. Any traffic traversing a link at the time of failure can be lost, and service may be interrupted. To mitigate this loss, a backup path can be established alongside the primary path requested by a user. If the user indicates that a pipe must be provisioned using Total survivability, a link-disjoint path pair will be found between the requested pipe’s two junctions. No links will be shared between these primary and backup paths. Each path in the pair will be palindromic; in other words, each will have both a forward/reverse, or AZ/ZA flow. If a disjoint path pair through the network cannot be found, then the request fails. 

.. _surv_pce_mpls:

MPLS-Layer Survivability
------------------------
    
Figure 16: Service Layer abstraction of MPLS-only Link-Disjoint survivability. Each MPLS service layer link represents a disjoint path pair through the MPLS layer.

Alternatively, if the user only needs disjoint paths in the MPLS layer, or Partial survivability, a service layer topology is constructed through the same approach used for non-palindromic requests. The only difference is that each undirectional logical link represents a disjoint path pair in that direction. For simplicity, the logical link in the reverse direction must be palindromic with the forward direction link; in other words, the forward logical link represents a disjoint pair of forward paths, while the reverse direction logical link represents that same path pair, but with the links in reverse. If no disjoint path pairs through the MPLS layer can be found, and it is not possible to establish a route without going through the MPLS layer, then the request fails. 

All Ethernet devices/ports/links used in the forward path, **must** be represented in the return path. Only MPLS devices/ports/links may differ in forward-/reverse-direction paths. This is accomplished by creating a :doc:`virtual_surv_topo_doc` for each *MPLS-Layer Survivable* request. This Service-Layer topology abstracts out the MPLS-Layer devices, and replaces them with a pair of abstract links between each pair of Service-Layer elements adjacent to the MPLS-Layer. These abstract links are computed independently such they they each correspond to the least-cost pair of link-disjoint MPLS-Layer paths between the end-points. Therefore, the forward and return paths *may* contain different routing elements and may not produce the same survivable solutions as one another.


.. _surv_pce_kpath:

K-Disjoint Path Survivability
-----------------------------
    
The user can request any number of disjoint paths for either Total or Partial survivability. This is implemented through an iterative version of Bhandari’s Algorithm, which will find the specified number of link-disjoint paths K (or the maximum number of disjoint paths less than K).

.. warning::

	As of February 1, 2017, K-path survivability is implemented only in the BhandariPCE, and there is not yet any avenue in the Survivability PCE API to support the service at this time. This connective tissue will be incorporated into a future release version.


.. toctree::
   :hidden:

   virtual_surv_topo_doc
