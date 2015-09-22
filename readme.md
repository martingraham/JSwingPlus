# JSwingPlus

JSwingPlus is an extension to Java Swing functionality that demonstrates visualisation techniques applied on top of standard Swing widgets.

The most notable are extensions to JTable that allow Parallel Coordinates and Scatterplot matrices to be shown, but with the column drag/drop and resizing capabilities that JTable already has. Other extensions allow JTables to be dynamically animated and allow variable length cells.

JGraph is yet another graph visualisation, but uses multi-threading to speed up layout calculations on multicore machines.

To run the build.xml will require your own jarsign.properties file (for jar signing) and for Apache Ant & Ivy to be installed, plus some fiddling about with the Ivy local repository filepath (see the .xml files), but the only essential dependency in the end is Log4J. It also uses an adapted version of Prefuse's RangeSlider component.

Caveat: This code worked as of Java 7, recent updates to Java have not been tested.

(C) Edinburgh Napier University

### Version
1.0