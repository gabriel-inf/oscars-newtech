
Getting Started
===============

Here you'll find instructions on obtaining, installing, and initializing OSCARS 1.0.

Download the sourcecode
-----------------------
The latest version of OSCARS can be downloaded via the project Github_.

.. _Github: https://github.com/esnet/oscars-newtech


Preparing Your Environment
--------------------------

Make sure the following are installed on your system:

* Java_ 1.8
* The latest version of Maven_ 

.. _Java: https://www.java.com
.. _Maven: http://maven.apache.org

Building with Maven
-------------------

To build the project, run the following commands from the main project directory:

.. code-block:: bash

   mvn install

This will require all unit tests included with the project to pass as a condition of a successful build. This is the recommended command.

Alternatively, you may skip these tests:

.. code-block:: bash

   mvn install -DskipTests

The unit tests can be run separately if desired:

.. code-block:: bash

   mvn test


Starting OSCARS Modules
-----------------------

OSCARS is pre-packaged with a start script, which initializes all modules of the system easily and conveniently. From the main project directory, run the command:

.. code-block:: bash

   ./bin/start.sh

At this point, OSCARS should be fully installed, built, and running on your local machine.  The following instructions will enable you to use the system, submit circuit reservations, etc.



Accessing the Web User Interface
--------------------------------

The Web UI serves as a visual portal through which the user can interact with the underlying OSCARS system.

The webui can be accessed (only once the system is running) at: https://localhost:8001. 

The user will be prompted for login credentials. By default, the following credentials will provide admin access priveleges:
 
   **Username: admin**

   **Password: oscars**


