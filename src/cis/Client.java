package cis;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * This class is the client side for a server based on UDP/IP.
 * The client communicates with the intermediate host at the specified port.
 * Last edited January 16th, 2018
 * @author Mohamed Dahrouj Lava Tahir
 *
 */
public class Client implements Runnable{
	private DatagramSocket socket;
	private byte[] readRequest;
	private byte[] writeRequest;
	private byte[] invalidRequest;
	
	private final String filename = "test.txt";
	private final String octet = "ocTEt";
	private byte[] fileNameBytes;
	private byte[] mode;

	private Request requestType;
	ByteArrayOutputStream byteArrayOutputStream;
	
	private InetAddress address;
	
	public Client(Request requestType) {
		this.requestType = requestType;
		try {
			// Construct a datagram socket and bind it to any available
	        // port on the local host machine. This socket will be used to
	        // send and receive UDP Datagram packets.
			socket = new DatagramSocket();
			
			//Initialize octet and mode bytes
			fileNameBytes = filename.getBytes();
			mode = octet.getBytes();
			
			//Initialize single byte array output stream
			//Format Read, Write and Invalid Requests
			//Reset once done
			byteArrayOutputStream = new ByteArrayOutputStream();
			this.formatReadRequest();
			this.formatWriteRequest();
			this.formatInvalidRequest();
			byteArrayOutputStream.reset();
			
			//Initialize address
			address = InetAddress.getLocalHost();
		}
		catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Read request format as per specification
	 * @throws IOException
	 */
	private void formatReadRequest() throws IOException {
		byteArrayOutputStream.reset();
		byteArrayOutputStream.write(0);
		byteArrayOutputStream.write(1);
		byteArrayOutputStream.write(fileNameBytes);
		byteArrayOutputStream.write(0);
		byteArrayOutputStream.write(mode);
		byteArrayOutputStream.write(0);
		
		readRequest = byteArrayOutputStream.toByteArray();
	}
	
	/**
	 * Write request format as per specification
	 * @throws IOException
	 */
	private void formatWriteRequest() throws IOException{
		byteArrayOutputStream.reset();
		byteArrayOutputStream.write(0);
		byteArrayOutputStream.write(2);
		byteArrayOutputStream.write(fileNameBytes);
		byteArrayOutputStream.write(0);
		byteArrayOutputStream.write(mode);
		byteArrayOutputStream.write(0);
		
		writeRequest = byteArrayOutputStream.toByteArray();
	}
	
	/**
	 * Invalid request format
	 * @throws IOException
	 */
	private void formatInvalidRequest() throws IOException {
		byteArrayOutputStream.reset();
		byteArrayOutputStream.write(0);
		byteArrayOutputStream.write(3);
		byteArrayOutputStream.write(fileNameBytes);
		byteArrayOutputStream.write(0);
		byteArrayOutputStream.write(mode);
		byteArrayOutputStream.write(0);
		
		invalidRequest = byteArrayOutputStream.toByteArray();
	}
	
	/**
	 * Process the requests then send and receive
	 * @param requestType Request type maybe read, write or invalid
	 */
	private void processRequest(Request requestType) {
		byte[] request;	
		if(requestType == Request.READ) {
			request = readRequest;
			System.out.println("\nClient: Read Request");
		}
		else if(requestType == Request.WRITE) {
			request = writeRequest;
			System.out.println("\nClient: Write Request");
		}
		else { //Invalid Request
			request = invalidRequest;
			System.out.println("\nClient: Invalid Request");
		}
		
		//Send the packet given request, address and port
		DatagramPacket packet = new DatagramPacket(request, request.length, address, Resources.clientPort);
		System.out.println("Client: Sending packet to intermediate host:");
		Resources.printPacketInformation(packet);
		Resources.sendPacket(packet, socket);
		System.out.println("Client: Packet sent!\n");
		
		//Process the received packet from socket
		DatagramPacket receivedPacket = Resources.receivePacket(socket);
		System.out.println("Client: Packet received:");
		Resources.printPacketInformation(receivedPacket);
	}

	@Override
	public void run(){
		this.processRequest(this.requestType);
	}
	/**
	 * Execute the client to send and receive requests to the port
	 * @param args Arguments
	 * @throws InterruptedException
	 */
	public static void main(String[] args) {
		/**
		 * Send 5 read, 5 write and 1 invalid requests to intermediate host.
		 * And receive
		 */
		for (int counter = 1; counter<= 10; counter++) {
			if(counter %2 == 0 ) {
				Thread readReqThread = new Thread(new Client(Request.READ));
				readReqThread.start();
			}
			else {
				Thread writeReqThread = new Thread(new Client(Request.WRITE));
				writeReqThread.start();
			}
			// Slow things down (wait 5 seconds)
			try {
				Thread.sleep(2500);
			} catch (InterruptedException e ) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		Thread invalidReqThread = new Thread(new Client(Request.INVALID));
		invalidReqThread.start();

	}

}