Author: Mohamed Hozayen, MohamedHozayen@cmail.carleton.ca
Systems and Computer Engineering Department,Carleton Univeristy
Supervisors: Dr. James Green, Dr. Shermeen Nizami
-----------------------------------------------------------------------------------------------------------------

PMDI: Patient Monitor Data Import Application
Drager XLDelta Patient Monitor is the machine used to import data from. 

The way the app works is 
1. the user selects a serial port which the patient monitor is connected to
2. open the selected serial port
3. The read data is automatically being saved in a Data folder in a CSV file format named by the time 
and date the reading started. This Data folder will be created in the same directory where the app is at.
4. Use ParserPMDI to parse the hexadecimal file after importing is done. Simply select the file that need to be
parsed and the parsed data is saved in a a folder called Parsed Data


App specifications: 
1- Install Java SE Development Kit 8 Update 131 32 bit:
	http://www.filepuma.com/download/java_development_kit_32bit_8.0.1310.11-14919/
	
2- comm.jar, win32com.dll, javax.comm.properties:
comm.jar is placed in C:\Program Files (x86)\Java\jre1.8.0_131\lib\ext
javax.comm.properties is placed in C:\Program Files (x86)\Java\jre1.8.0_131\lib
win32com.dll is placed in C:\Program Files (x86)\Java\jre1.8.0_131\bin
It should be already in REQUIREMENTS if not:
	https://drive.google.com/drive/folders/0BzPNFkK3y-4oQTlucDFEb2tJc0k
	
set PATH=C:\Program Files (x86)\Java\jre1.8.0_131/bin;%PATH%
	