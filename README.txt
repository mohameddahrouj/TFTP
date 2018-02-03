Mohamed Dahrouj, Ali Farah, Lava Tahir, Tosin Oni, Vanja Vesolinovic
Student Number: 100951843
Due: January 19 @ 23:59

******SYSC 3303 - Assignment 1******

******File Structure******
Package cis stands for client-intermediatehost-server.

There are 5 java files within the source code.
1. Client.java : Represents the client class which interacts with intermediate host
2. ErrorSimulator.java : Represents the intermediate host which interacts between client and server
3. Server.java : Represents the server which sends and receives to the intermediate host
4. Request.java: Is an enumerated class that represents the request types of the packets in the simulation
5. Resources.java : Is a common class that is used by all the Client, ErrorSimulator and Server to print, send and receive packets.
6. WriteHandler.java: This class will send the contents of a file to the receiver.
7. ReadHandler.java: This class will receive data from a file and write it to a local file.
8. Handler.java: Is the base class of the WriteHandler and ReadHandler.

******Set-up Instructions******
1. Start the Server (main)
2. Start the Intermediate Host (main)
3. Start the Client (main)
4. After starting the Client you will be prompted to enter the request type. Type R for a read request and W for a Write Request.
5. Then you will be prompted to enter the path of a file.
6a. If you enter a read request then the contents of the file that you specified will be transferred to the temp.txt file. The temp.txt can be found in the same folder as the README.
6b. If you enter a write request then the contents of the temp.txt will be transferred to the file that you specified.
7. The server and the ErrorSimulator will shutdown after 100s of inactivity.

******Submitted Images******
1. UMLClassDiagram.png: Represents requirement #4 and shows all the classes and their dependancies
2. 3 Collaboration Diagrams for each class  
3. UCM Diagram of the simulation

No tests were created.