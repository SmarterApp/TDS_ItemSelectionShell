# Welcome to the Item Selection Shell Application

The ItemSelectionShell project is group of modules which can be used for item selection in the Student tests. 

## License ##
This project is licensed under the [AIR Open Source License v1.0](http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf).

## Getting Involved ##
We would be happy to receive feedback on its capabilities, problems, or future enhancements:

* For general questions or discussions, please use the [Forum](http://forum.opentestsystem.org/viewforum.php?f=9).
* Use the **Issues** link to file bugs or enhancement requests.
* Feel free to **Fork** this project and develop your changes!

## Module Overview

### tds-itemselection-common

   tds-itemselection-common contains implementation of the common classes like TestItem, ItemGroup, Dimension, and IRTMeasures that can be reused by the other projects, contains interface class IAIROnline.java for integration with the Student project and interfaces of the different selection algorithms (Fixed Form, Field Test and Adaptive algorithms). This module also contains implementations of the IRT Models classes.

### tds-itemselection-aironline

  tds-itemselection-aironline module contains implementations for AIROnline and  selection algorithms.

### tds-itemselection-impl

  tds-itemselection-impl module contains all the common classes that are needed for selection algorithms implementations.



## Setup
In general, build the code and deploy the JAR file.


### Build order

If building all components from scratch the following build order is needed:

* shared-db
* tdsdll


## Dependencies
ItemSelectionShellDev has a number of direct dependencies that are necessary for it to function.  These dependencies are already built into the Maven POM files.

### Compile Time Dependencies

* shared-db
* tds-dll-mysql


### Test Dependencies
* shared-db-test
* junit
* jcl-over-slf4j
* slf4j-log4j12
* c3p0
* log4j