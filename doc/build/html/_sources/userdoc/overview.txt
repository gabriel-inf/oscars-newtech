
Project Overview
================

OSCARS 1.0 has been developed as a Springboot application, made up of two major components: 
- The main application ("core"). 
- The web user interface ("webui"). 

The main project directory is structured as follows:

- **/bin**: Contains scripts and executables for running OSCARS.
- **/check**: Integration tests.
- **/core**: The main OSCARS application. Handles reservation requests, determines which path (if any) is available to satisfy the request, and reserves the network resources. Key modules include:
	- **acct**: Maintains list of customers and handles storing, editing, and retrieving account information.
	- **authnz**: Tracks permissions/authorization associated with user accounts.
	- **bwavail**: Calculates the minimum amount of bandwidth available between a given source and destination within a given time duration. Returns a series of time points and corresponding changes in 		availability, based on reservations in the system. 
	- **conf**: Retrieves configurations, specified in "oscars-newtech/core/config", for each OSCARS module on startup.
	- **helpers**: Functions useful for dealing with Instants and VLAN expression parsing.
	- **pce**: The Path Computation Engine. It takes a requested reservation's parameters, evaluates the current topology, determines the (shortest) path, if any, and decides which network resources must be reserved.
	- **pss**: Sets up, tears down, modifies and verifies network paths. Handles templates for network devices.
	- **resv**: Tracks reservations, and receives user parameters for reservation requests.
	- **servicetopo**: Abstracts the network topology to create unique “Service Level” views of the topology for a given request.
	- **tasks**: Services which run in the background and perform tasks at certain intervals (e.g. Select a submitted request to begin the reservation process).
	- **topo**: Maintain topology information.
- **/doc**: User and Code documentation.
- **/shared**: A collection of shared class definitions used by the different modules.
- **/webui**: The web interface through which users can view their current and past reservations, and submit new reservation requests. The WebUI is built using the Thymeleaf template engine. The WebUI is a portal through which a user communicates with the Core API through REST calls.
- **/whatif**: Classes to facilitate flexible circuit provisioning. Services include translating incomplete or loosely-defined input parameters into optional or alternative solutions which may have different characteristics, service enhancements, and costs.


Project Module Details
----------------------

.. toctree::
   :titlesonly:

   core_doc
   webui_doc
   whatif_doc
