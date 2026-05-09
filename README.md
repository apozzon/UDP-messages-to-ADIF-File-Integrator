# UDP-messages-to-ADIF-File-Integrator
This app has been developed in JAVA using Eclipse IDE.
It is designed for hamradio operators to integrate logging data from applications like MSHV, JTDX, WSJT, Decodium etc. integrate them with other data and send to a logger like Log4OM2.
The way it wirks is:
Listens for UDP packets on a configurable port Filters packets containing ADIF (Amateur Data Interchange Format) data 
Modifies the ADIF data by adding other ADIF fields (i.e. satellite-related)
Writes the processed data to an ADIF file in a specific directory. It's designed for hamradio operators to integrate logging data from applications like Log4OM2.

In the directories yo can find the source code and the compiled program you can install on your Windows PC. Pre-requiite is to have Java Virtual Machine installed on your PC.
