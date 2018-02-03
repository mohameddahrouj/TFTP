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
			String file = getFile(receivedPacket);
            Thread serverSendingThread = new Thread(new ServerResponse(receivedPacket, requestType,file));
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
	 * Gets the path of the file from the packet
	 * @param packet the packet received from the Client
	 * @return the path of the file
	 */
	private String getFile(DatagramPacket packet) {

		byte[] data = packet.getData();
		int index = 2;

		while(data[index] != 0)
		{
			index++;
		}

		return new String(data,2,index-2);
	}


	/**
	 * Determine if packet is a read or write request
	 * @param packet Packet
	 * @return Request type of packet
	 */
	private Request packetRequestType(DatagramPacket packet) throws Exception {
		Request request = Resources.packetRequestType(packet);
		if(Request.INVALID == request)
			throw new Exception("Invalid Packet Received");
		return request;
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