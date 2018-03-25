package cis.host;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import cis.host.Operation.Mode;
import cis.utils.Request;
import cis.utils.Resources;

/**
 * Error Simulator is the intermediary between client and server Last edited
 * January 30th, 2018
 * 
 * @author Mohamed Dahrouj, Ali Farah, Lava Tahir, Tosin Oni, Vanja Veselinovic
 *
 */
public class ErrorSimulator {
	DatagramSocket clientSocket;
	DatagramSocket serverSocket;
	InetAddress address;
	Operation operation;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	private Map<Integer, Integer> connections;

	public ErrorSimulator(Operation operation) {
		try {
			this.connections = new HashMap<>();
			// Initialize client socket at the shared port
			clientSocket = new DatagramSocket(Resources.clientPort);
			// Initialize server socket at any port
			serverSocket = new DatagramSocket();
			serverSocket.setSoTimeout(Resources.timeout);
			// Initialize address
			address = InetAddress.getLocalHost();
			this.operation = operation;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Send and receive requests between client and server
	 */
	public void sendAndReceive() {

		int clientPort = this.forwardClientPacket();
		this.forwardServerPacket(clientPort);
	}

	/**
	 * Forward the client packet to the server
	 */
	private int forwardClientPacket() {
		// Receive request from client
		System.out.println("Waiting to receive a request from client...");
		int port = 0;
		DatagramPacket receivedPacket;
		try {
			receivedPacket = Resources.receivePacket(clientSocket);
			// Process the received packet from client socket
			System.out.println("Error Simulator: Packet received:");
			Resources.printPacketInformation(receivedPacket);

			int serverPort = getServerPort(receivedPacket);

			// Form new packet from received packet
			System.out.println("\nError Simulator: Forming new Packet:");
			DatagramPacket newPacket = new DatagramPacket(receivedPacket.getData(), receivedPacket.getData().length,
					address, serverPort);
			// Send the newly formed packet to server
			Operation.Mode mode = operation.sendPacket(newPacket, serverSocket);
			System.out.println("\nError Simulator: Sending packet to server:");
			if (listenOnSamePort(mode,operation.type))
			{
				this.forwardClientPacket();
			}

			port = receivedPacket.getPort();
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return port;
	}

	/**
	 * Get server port number based on request type
	 */
	private int getServerPort(DatagramPacket packet) {

		Request request = Resources.packetRequestType(packet);
		int clientPort = packet.getPort();

		// if the packet is an ACK/DATA packet then find the port
		if (request != Request.WRITE && request != Request.READ) {
			if (connections.containsKey(clientPort)) {
				return connections.get(clientPort);
			}
		}

		connections.put(clientPort, -1);
		return Resources.serverPort;
	}

	/**
	 * Forward the server packet to the client
	 */
	private void forwardServerPacket(int clientPort) {
		// Receive response packet from the server
		System.out.println("Error Simulator: Waiting for packet from server\n");
		DatagramPacket receivedServerPacket;
		try {
			receivedServerPacket = Resources.receivePacket(serverSocket);
			System.out.println("Error Simulator: Packet received from server:");
			Resources.printPacketInformation(receivedServerPacket);

			// save the port the server port
			this.connections.put(clientPort, receivedServerPacket.getPort());

			// Create new packet to send to client
			DatagramPacket sendPacket = new DatagramPacket(receivedServerPacket.getData(),
					receivedServerPacket.getData().length, address, clientPort);
			System.out.println("\nIntermediate Host: Sending packet to client");
			Operation.Mode mode = operation.sendPacket(sendPacket, clientSocket);
			if (listenOnSamePort(mode,operation.type))
			{
				this.forwardServerPacket(clientPort);
			}
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	
	
	private boolean listenOnSamePort(Mode mode, Request request)
	{
		return (mode == Mode.LOSE || mode == Mode.DELAY && operation.type != Request.ACK);
	}

	/**
	 * Execute the Error Simulator
	 * 
	 * @param args Arguments
	 */
	public static void main(String[] args) {
		ErrorSimulator es = new ErrorSimulator(new Operation());
		// Listen forever...
		while (true) {
			es.sendAndReceive();
		}
	}

}