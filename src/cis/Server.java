package cis;

import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Server communicates with the intermediate host
 * Last edited January 30th, 2018
 * @author Mohamed Dahrouj, Lava Tahir
 *
 */
public class Server {
	
	private DatagramSocket serverSocket;
	private InetAddress address;
	private byte[] readRequest;
	private byte[] writeRequest;
	ByteArrayOutputStream byteArrayOutputStream; 
	public Server() {
		try {
			//Initialize sockets
			serverSocket = new DatagramSocket(Resources.serverPort);
			serverSocket.setSoTimeout(Resources.timeout);
			//Initialize address
			address = InetAddress.getLocalHost();
			
			//Initialize single byte array output stream
			//Format Read and Write Requests
			//Reset once done
			byteArrayOutputStream = new ByteArrayOutputStream();
			this.formatReadRequest();
			this.formatWriteRequest();
			byteArrayOutputStream.reset();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Read request format as per specification
	 */
	private void formatReadRequest() {
		byteArrayOutputStream.reset();
		byteArrayOutputStream.write(0);
		byteArrayOutputStream.write(3);
		byteArrayOutputStream.write(0);
		byteArrayOutputStream.write(1);
		
		readRequest = byteArrayOutputStream.toByteArray();
	}
	
	/**
	 * Write request format as per specification
	 */
	private void formatWriteRequest() {
		byteArrayOutputStream.reset();
		byteArrayOutputStream.write(0);
		byteArrayOutputStream.write(4);
		byteArrayOutputStream.write(0);
		byteArrayOutputStream.write(0);
		
		writeRequest = byteArrayOutputStream.toByteArray();
	}
	
	/**
	 * Receive packet from intermediate host
	 */
	public void receive(){
		try {
            System.out.println("Waiting to receive a request from intermediate host...");
            DatagramPacket receivedPacket = Resources.receivePacket(serverSocket);
            System.out.println("Server: Packet received:");
            Resources.printPacketInformation(receivedPacket);
            Request requestType = packetRequestType(receivedPacket);
            //create thread to handle response

            Thread serverSendingThread = new Thread(new ServerResponse(receivedPacket, requestType));
            serverSendingThread.start();
        }
        catch (Exception e)
        {
            this.serverSocket.close();
            e.printStackTrace();
            System.exit(1);
        }
	}


    /**
     * Determine if packet is a read or write request
     * @param packet Packet
     * @return Request type of packet
     */
    private Request packetRequestType(DatagramPacket packet) throws Exception {
        //The second element of a read request should be 1
        //Whereas the second element of a write request should be 2
        if(packet.getData()[1] == (byte) 0x01) {
            return Request.READ;
        }
        else if(packet.getData()[1] == (byte) 0x02) {
            return Request.WRITE;
        }
        throw new Exception("Invalid Packet Received");
    }

	/**
	 * Execute server to communicate with intermediate host
	 * @param args Arguments
	 */
	public static void main(String[] args) {
		Server server = new Server();
		while(true) {
			server.receive();
		}
	}

}