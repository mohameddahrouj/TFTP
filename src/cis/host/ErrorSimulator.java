package cis.host;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
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
	InetAddress serverIPaddress;
	Operation operation;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	private Map<Integer, Integer> connections;
	private Scanner inputScanner;
	
	public ErrorSimulator() {
		try {	
			this.inputScanner = new Scanner(System.in);
			this.operation = new Operation(inputScanner);
			this.connections = new HashMap<>();
			// Initialize client socket at the shared port
			clientSocket = new DatagramSocket(Resources.errorSimulatorPort);
			// Initialize server socket at any port
			serverSocket = new DatagramSocket();
			serverSocket.setSoTimeout(Resources.timeout);
			// Initialize address
			address = InetAddress.getLocalHost();
			serverIPaddress = this.getIPAddress();
			this.inputScanner.close();

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
			new Thread(new Runnable() {
				public void run()
				{
					while (true)
					{
						forwardClientPacket();
					}
				}
			}).start();
			new Thread(new Runnable() {
				public void run()
				{
					while (true)
					{
						forwardServerPacket(clientPort);
					}
				}
			}).start();
		
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
					serverIPaddress, serverPort);
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

		int clientPort = packet.getPort();

		if (this.connections.containsKey(clientPort)) {
			return this.connections.get(clientPort);
		}
		
		this.connections.put(clientPort, -1);
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
		return (mode == Mode.LOSE || mode == Mode.DELAY) && operation.type != Request.ACK;
	}
	
	private InetAddress getIPAddress() {
		System.out.println("Please Enter IP Address: ");
		while (true) {
			String ip = inputScanner.nextLine();
			if (ip.length() != 0) {
				try {
					return InetAddress.getByName(ip);
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			System.out.println("Not a valid ip. Please reenter the IP address.");
		}
	}

	/**
	 * Execute the Error Simulator
	 * 
	 * @param args Arguments
	 */
	public static void main(String[] args) {
		ErrorSimulator es = new ErrorSimulator();
		// Listen forever...
		es.sendAndReceive();
	}

}