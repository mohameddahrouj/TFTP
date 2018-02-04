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


### Set-up Instructions
1. Execute the Server.java in server folder (main)
2. Execute the ErrorSimulator.java in host folder (main)
3. Execute the Client.java in client folder (main)
4. After starting the Client you will be prompted to enter the request type. Type `R` for a read request or `W` for a write request.
5. Then you will be prompted to enter the path of a file.
6a. If you enter a read request then the contents of the file that you specified will be transferred to the temp.txt file. The temp.txt can be found in the same folder as the README.
6b. If you enter a write request then the contents of the temp.txt will be transferred to the file that you specified.
7. The server and the ErrorSimulator will shutdown after 100s of inactivity.

### Included  Images
1. UMLClassDiagram.png: Represents requirement #4 and shows all the classes and their dependencies
2. UCMRead.png: UCM diagram of a read request
3. UCMWrite.png: UCM diagram of a write request


| Deliverable   | Due Date    |
|---------------|-------------|
| Iteration 1   | February 3  |
| Iteration 2   | February 17 |
| Iteration 3   | March 10    |
| Iteration 4   | March 24    |
| Final Project | April 11    |

No tests were created.