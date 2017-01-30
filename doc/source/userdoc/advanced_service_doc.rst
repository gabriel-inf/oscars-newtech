
Path Computation Service Enhancements
=====================================

.. image:: ../.static/construction.png
   :scale: 50%
   :align: center

OSCARS 1.0 supports a number of all-new composable routing capabilities which provide an unprecedented amount of user control over how the :doc:`pce_doc` identifies working solutions. All of these services may be triggered without alteration to the underlying code, and without the need for modifying configuration parameters when launching OSCARS. This document discusses the PCE service enhancement suite, while :doc:`???` provides details on how the user can exert control over the flow through the PCE to engage and toggle each of the described services. The following table offers a listing of the services discussed throughout the remainder of the document. Note: Forward-direction circuit route is denoted as **A-Z**, while reverse is denoted as **Z-A**.


==========================================   	============
Service Enhancement   			  	Description
==========================================	============
:ref:`asymm_pce_service`			A-Z and Z-A circuits may exhibit/explicitly require different bandwidth quantities.
:ref:`nonpalindromic_pce_service`		A-Z and Z-A circuits may not mirror one another. Only portions of the route passing through an MPLS segment may differ.
:ref:`multipoint_pce_service`			Provide more than one destination device for a circuit, or create a cyclic transfer among a set of devices.
:ref:`ero_pce_services`				Force the PCE to use or avoid specific portions of the network during pathfinding.	  
- :ref:`ero_pce_complete`			Specify exact A-Z and Z-A circuits in which the specified path must be traversed in order. Pathfinding fails if exact match cannot be reserved.
- :ref:`ero_pce_partial`			Specify a partial A-Z or Z-A circuit in which all specified nodes/ports must be reached in relative order, but they serve as waypoints along the computed path.
- :ref:`ero_pce_blacklist`			Specify a set of nodes/ports which are to be pruned from the network topology prior to pathfinding to ensure avoidance.
:ref:`surv_pce_services`			Establish multiple routes along disjoint-paths within the same requested pipe in case of physical network failure. If survivable routing is impossible, the entire reservation fails.
- :ref:`surv_pce_complete`			*Only* the souce node/port and destination source/port are shared among paths. **Exactly two paths computed.**
- :ref:`surv_pce_mpls`				Provides survivability *only*	within an MPLS segment. All Ethernet segments are identical among paths. **Exactly two paths computed.**
- :ref:`surv_pce_kpath`				Specify a number (K) of disjoint paths desired. All paths will be set-wise disjoint.
:ref:`vlan_translation`				Enables multiple VLAN tags to be used for a single circuit by translating VLAN tags to those available at intermediate nodes along the route.
==========================================	============

.. toctree::
   :maxdepth: 1

   enhanced/asymm_service_doc
   enhanced/nonpalindromic_service_doc
   enhanced/multipoint_service_doc
   enhanced/ero_service_doc
   enhanced/survivability_service_doc
   enhanced/vlan_service_doc
   virtual_surv_topo_doc



.. _surv_pce_services:

Survivable Routing Services
---------------------------

.. _surv_pce_complete:

Complete Path Survivability
^^^^^^^^^^^^^^^^^^^^^^^^^^^

Figure 15: End-to-End (excluding the device -> fixture links) link-disjoint Primary and Backup paths.

Network elements, particularly links, experience failures almost regularly. Any traffic traversing a link at the time of failure can be lost, and service may be interrupted. To mitigate this loss, a backup path can be established alongside the primary path requested by a user. If the user indicates that a pipe must be provisioned using Total survivability, a link-disjoint path pair will be found between the requested pipe’s two junctions. No links will be shared between these primary and backup paths. Each path in the pair will be palindromic; in other words, each will have both a forward/reverse, or AZ/ZA flow. If a disjoint path pair through the network cannot be found, then the request fails. 

.. _surv_pce_mpls:

MPLS Segment Survivability
^^^^^^^^^^^^^^^^^^^^^^^^^^
    
Figure 16: Service Layer abstraction of MPLS-only Link-Disjoint survivability. Each MPLS service layer link represents a disjoint path pair through the MPLS layer.

Alternatively, if the user only needs disjoint paths in the MPLS layer, or Partial survivability, a service layer topology is constructed through the same approach used for non-palindromic requests. The only difference is that each undirectional logical link represents a disjoint path pair in that direction. For simplicity, the logical link in the reverse direction must be palindromic with the forward direction link; in other words, the forward logical link represents a disjoint pair of forward paths, while the reverse direction logical link represents that same path pair, but with the links in reverse. If no disjoint path pairs through the MPLS layer can be found, and it is not possible to establish a route without going through the MPLS layer, then the request fails. 

.. _surv_pce_kpath:

K-Disjoint Path Survivability
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
    
The user can request any number of disjoint paths for either Total or Partial survivability. This is implemented through an iterative version of Bhandari’s Algorithm, which will find the specified number of link-disjoint paths K (or the maximum number of disjoint paths less than K).

.. warning::

	As of February 1, 2017, K-path survivability is implemented only in the BhandariPCE, and there is not yet any avenue in the Survivability PCE API to support the service at this time. This connective tissue will be incorporated into a future release version.



.. _vlan_translation:

VLAN Translation/Swapping
-------------------------

Figure 17: VLAN Translation example where a different VLAN tag can be assigned to intermediate ports if a requested tag is not available.

While not a specific service that can be requested, VLAN Translation (or Swapping), is a procedure used in the TranslationPCE to assign VLAN tags to a reserved path. VLAN translation allows one VLAN tag, possibly different than the tag(s) requested, to be used for the intermediate ports in a path. The VLANs available at each port (or at each switch) are encapsulated in a set of IntRange objects, each of which represents a contiguous range of VLAN IDs. For example, IntRanges [5, 10] and [15, 20] could be available at a specific port, while IDs outside of that range are unavailable. To perform VLAN swapping, the PCE take the path returned from pathfinding and retrieve the available ranges from each port involved in the path (along with each port on each switch in the path, if those ports aren't already in the path). The problem ends up being similar to the Set Cover problem - choose the minimum set of VLANs that will cover all involved ports, with the following restrictions: fixtures(ingress/egress points) must use one of the requested VLAN tags (could be different for different fixtures), all intermediate ports along the path must use the same VLAN tag, and all ports on any switches involved must use the same VLAN tag.

Major steps:
(1) Check if there are any VLAN tags available across the path (including fixtures). If so, use one of those.
(2) If (1) was not successful: For each fixture, assign a requested & available VLAN tag. Check if any of those tags are available across the intermediate ports of the path. If so, assign it to those ports as well (if they have not have VLANs assigned yet). If any fixture cannot have an assignment, then the request cannot be provisioned.
(3) If the intermediate ports are still not assigned, determine if there are any tags that are available across the entire intermediate path. If so, assign them. If not, the request cannot be provisioned. 

.. note:: 

   Due to the above requirement that all ports on a switch are assigned a tag if that tag is assigned to any one of the ports, multiple VLANs may be assigned to a port throughout this process.

