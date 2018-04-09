package cis.server;

import java.io.ByteArrayOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import cis.utils.Request;
import cis.utils.Resources;

/**
 * Server communicates with the error simulator.
 * 
 * If read request, server will check if specified file name exists on the
 * server side. If yes, then the server will create then write to the same file
 * name on the client side. If no, file not found error packet will be sent. If
 * the file name already exists on the client side, file already exists error
 * packet will be sent.
 * 
 * 
 * If write request, server will check if specified file name exists on the
 * client side. If yes, then the server will create then write to the same file
 * name on the server side. If no, then file not found error will be sent. If
 * the file name already exists on the server side, file already exists error
 * packet will be sent. If the file name has read only on server side, access
 * violation packet will be sent.
 * 
 * Last edited January 30th, 2018
 * 
 * @author Mohamed Dahrouj, Ali Farah, Lava Tahir, Tosin Oni, Vanja Veselinovic
 *
 */
public class Server {

	private DatagramSocket serverSocket;
	ByteArrayOutputStream byteArrayOutputStream;

	public Server() {
		try {
			// Initialize sockets
			serverSocket = new DatagramSocket(Resources.serverPort);
			serverSocket.setSoTimeout(Resources.timeout);

			// Initialize single byte array output stream
			// Format Read and Write Requests
			// Reset once done
			byteArrayOutputStream = new ByteArrayOutputStream();
			byteArrayOutputStream.reset();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Receive packet from intermediate host
	 */
	public void receive() {
		while (true) {
			try {
				System.out.println("Listening on port: " + this.serverSocket.getLocalPort());
				DatagramPacket receivedPacket = Resources.receivePacket(serverSocket);
				System.out.println("Server: Packet received:");
				Resources.printPacketInformation(receivedPacket);
				Request requestType = packetRequestType(receivedPacket);
				// create thread to handle response
				String file = getFile(receivedPacket);
				Thread serverSendingThread = new Thread(new ServerResponse(receivedPacket, requestType, file));
				serverSendingThread.start();
				System.out.println("New thread has started\n\n.");
			} catch (Exception e) {
				this.serverSocket.close();
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	/**
	 * Gets the path of the file from the packet
	 * 
	 * @param packet
	 *            the packet received from the Client
	 * @return the path of the file
	 */
	private String getFile(DatagramPacket packet) {

		byte[] data = packet.getData();
		int index = 2;

		while (data[index] != 0) {
			index++;
		}

		return new String(data, 2, index - 2);
	}

	/**
	 * Determine if packet is a read or write request
	 * 
	 * @param packet
	 *            Packet
	 * @return Request type of packet
	 */
	private Request packetRequestType(DatagramPacket packet) throws Exception {
		Request request = Resources.packetRequestType(packet);
		if (Request.READ != request && Request.WRITE != request)
			throw new Exception("Invalid Packet Received");
		return request;
	}

	/**
	 * Execute server to communicate with intermediate host
	 * 
	 * @param args
	 *            Arguments
	 */
	public static void main(String[] args) {
		Server server = new Server();
		System.out.println("Waiting to receive a request from intermediate host...");
		server.receive();
	}

}