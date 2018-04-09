# TFTP
SYSC 3303 Project - Trivial File Transfer Protocol System

| Name              | Student Number | Responsibilites   |
|-------------------|----------------|-------------------|
| Ali Farah         | 100937214      | Code				 |
| Lava Tahir        | 100933449      | Code				 |
| Mohamed Dahrouj   | 100951843      | Code				 |
| Tosin Oni         | 100961759      | Code				 |
| Vanja Veselinovic | 100946517      | Code and diagrams |


### File Structure
There are 5 folders within the source code.

Client:

    Client.java : Represents the client class which interacts with the system


Host:
	 
	 ErrorSimulator.java : Represents the intermediate host which interacts between client and server and simulates errors.

Server:
	 
	 Server.java : Represents the server which sends and receives to the intermediate host.

Handlers:
	 
	 Handler.java: Is the base class of the WriteHandler and ReadHandler.
	 WriteHandler.java: This class will send the contents of a file to the receiver.
	 ReadHandler.java: This class will receive data from a file and write it to a local file.

Utils:
	 
	 Request.java: Is an enumerated class that represents the request types of the packets in the simulation
	 Resources.java : Is a common class that is used by all the Client, ErrorSimulator and Server to print, send and receive packets.
	 IOErrorType.java : A class that enumerates the different IO error types.

### Set-up Instructions
1. Execute the Server.java in server folder (main)
2. Execute the ErrorSimulator.java in host folder (main)
3. Execute the Client.java in client folder (main)
4. After starting the Client you will be prompted to enter whether or not you want to delay/duplicate/ lose packet  
5. After starting the Client you will be prompted to enter the request type. Type `R` for a read request or `W` for a write request.
6. Then you will be prompted to enter the path of a file.
7a. If you enter a read request then the contents of the file that you specified will be transferred to a file of the same name in the Client folder.
7b. If you enter a write request then the contents of the file that you specified will be transferred to a file of the same name in the Server folder.
8. The client and the server response will timeout after 1s.
9. The server and the ErrorSimulator will shutdown after 300s of inactivity.

### Included  Images
1. UMLClassDiagram.png: UML class diagram that shows all the classes and their dependencies
2. UCMRead.png: UCM diagram of a read request
3. UCMWrite.png: UCM diagram of a write request
4. Error timing diagrams.png: Timing diagrams for all error scenarios for iteration 2. There is no diagram for a write request FileNotFound error because a request is not sent in the first place if the file does not exist on the client side.
5. Timing diagrams for lost ACK and DATA.png: Timing diagrams for lost ACK and DATA for iteration 3.
6. ack_delay.png and packet_delay.png: Timing diagrams for delayed ACK and DATA for iter 3.
7. ack_duplicate.png: Timing diagram for duplicate ACK for iter 3.

| Deliverable   | Due Date    |
|---------------|-------------|
| Iteration 1   | February 3  |
| Iteration 2   | February 17 |
| Iteration 3   | March 10    |
| Iteration 4   | March 24    |
| Final Project | April 11    |

No tests were created.
