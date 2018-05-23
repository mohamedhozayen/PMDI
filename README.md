# PMDI
PMDI is a software that imports patients data off a patient monitor type Draeger XDelta

Author: Mohamed Hozayen, MohamedHozayen@cmail.carleton.ca
Contiributors:
	Amente Bekele, amentebullo@cmail.carleton.ca

Systems and Computer Engineering Department, Carleton Univeristy

Supervisors: Dr. James Green, Dr. Shermeen Nizami

Project: Pressure Senstive Mats Studies in the NICU at CHEO
-----------------------------------------------------------------------------------------------------------------

PMDI: Patient Monitor Data Import Application
Drager XLDelta Patient Monitor is the machine used to import data from. 

Note: the PMDI is still under development 

The way the app works is 
1. the user selects a serial port which the patient monitor is connected to
2. open the selected serial port
3. The read data is automatically being saved in a Data folder in a CSV file format named by the time 
and date the reading started. This Data folder will be created in the same directory where the app is at.
4. Use ParserPMDI to parse the hexadecimal file after importing is done. Simply select the file that need to be
parsed and the parsed data is saved in a a folder called Parsed Data

## Development Requirements

Install the latest version of [IntelliJ IDEA Community](https://www.jetbrains.com/idea/download/#section=windows) 


## Runtime Requirments: 

1 - Install [Java SE Development Kit](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
	
2 - Download [RXTX native library for serial ports](http://rxtx.qbang.org/wiki/index.php/Download) 

3 - Unzip the RXTX download and copy the native library files "rxtxParallel.dll" and "rxtxSerial.dll" to the the path `JDK_INSTALL_PATH/jre/bin`. JDK_INSTALL_PATH should look like `C:\Program Files (x86)\Java\jdk1.8.0_60\jre\bin` depending on the JDK update version.

## Building the Application

Import the project "Patient Monitor Data Import" in Intellij. For testing during development run the file "Gui.java". **IMPORTANT** : Make sure to check the box `Include dependencies with "Provided" scope` under `Run --> Edit Configurations`
