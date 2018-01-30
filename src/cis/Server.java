package cis;

import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Server communicates with the intermediate host
 * Last edited January 16th, 2018
 * @author Mohamed Dahrouj
 *
 */
public class Server {
	
	private DatagramSocket serverSocket;
	private DatagramSocket sendingSocket;
	private InetAddress address;
	private byte[] readRequest;
	private byte[] writeRequest;
	ByteArrayOutputStream byteArrayOutputStream; 
	public Server() {
		try {
			//Initialize sockets
			serverSocket = new DatagramSocket(Resources.serverPort);
			sendingSocket = new DatagramSocket();
			
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
	 * Send received packet to intermediate host
	 * @throws Exception if packet invalid
	 */
	public void send(DatagramPacket receivedPacket) throws Exception {
		
		if(isPacketValid(receivedPacket)) {
			if(packetRequestType(receivedPacket)==Request.READ) {
				System.out.println("\nServer: Forming new read packet");
				DatagramPacket newPacket = new DatagramPacket(readRequest, readRequest.length, address,  receivedPacket.getPort());
				Resources.printPacketInformation(newPacket);
				Resources.sendPacket(newPacket, sendingSocket);
			}
			else if(packetRequestType(receivedPacket)==Request.WRITE) {
				System.out.println("\nServer: Forming new write packet");
				DatagramPacket newPacket = new DatagramPacket(writeRequest, writeRequest.length, address,  receivedPacket.getPort());
				Resources.printPacketInformation(newPacket);
				
				System.out.println("\nServer: Sending packet to intermediate host");
				Resources.printPacketInformation(newPacket);
				Resources.sendPacket(newPacket, sendingSocket);
				System.out.println("Server: Packet sent!\n");
			}
			
		}
		else {
			serverSocket.close();
			sendingSocket.close();
			throw new Exception("Invalid packet detected.");
			
		}    
	}
	
	/**
	 * Determine if packet is valid
	 * @param packet Packet to check
	 * @return True if packet is valid
	 */
	private boolean isPacketValid(DatagramPacket packet) {
		
		if(packetRequestType(packet)==Request.INVALID) {
			return false;
		}
		return true;
	}
	
	/**
	 * Determine if packet is a read or write request
	 * @param packet Packet
	 * @return Request type of packet 
	 */
	private Request packetRequestType(DatagramPacket packet) {
		//The second element of a read request should be 1
		//Whereas the second element of a write request should be 2
		if(packet.getData()[1] == (byte) 0x01) {
			return Request.READ;
		}
		else if(packet.getData()[1] == (byte) 0x02) {
			return Request.WRITE;
		}
		return Request.INVALID;
	}
	/**
	 * Receive packet from intermediate host
	 */
	public void receive(){
		System.out.println("Waiting to receive a request from intermediate host...");
		DatagramPacket receivedPacket = Resources.receivePacket(serverSocket);
		System.out.println("Server: Packet received:");
		Resources.printPacketInformation(receivedPacket);

		//create thread to handle response
		Thread serverSendingThread = new Thread(new ServerResponse(receivedPacket, readRequest, writeRequest,
				address, sendingSocket, serverSocket));
		serverSendingThread.start();
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