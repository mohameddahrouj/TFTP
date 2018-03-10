package cis.host;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

import cis.utils.Request;
import cis.utils.Resources;

/**
 * Error Simulator is the intermediary between client and server
 * Last edited January 30th, 2018
 * @author Mohamed Dahrouj, Ali Farah, Lava Tahir, Tosin Oni, Vanja Veselinovic
 *
 */
public class ErrorSimulator {
	DatagramSocket clientSocket;
	DatagramSocket serverSocket;
	InetAddress address;

	private Map<Integer, Integer> connections;

	public ErrorSimulator(){
		try {
			this.connections = new HashMap<>();
			//Initialize client socket at the shared port
			clientSocket = new DatagramSocket(Resources.clientPort);
			//Initialize server socket at any port
			serverSocket = new DatagramSocket();
			serverSocket.setSoTimeout(Resources.timeout);
			//Initialize address
			address = InetAddress.getLocalHost();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Send and receive requests between client and server
	 */
	public void sendAndReceive(){

		int clientPort = this.forwardClientPacket();
		this.forwardServerPacket(clientPort);
	}

	/**
	 * Forward the client packet to the server
	 */
	private int forwardClientPacket()
	{
		//Receive request from client
		System.out.println("Waiting to receive a request from client...");
		int port = 0;
		DatagramPacket receivedPacket;
		try {
			receivedPacket = Resources.receivePacket(clientSocket);
			//Process the received packet from client socket
			System.out.println("Error Simulator: Packet received:");
			Resources.printPacketInformation(receivedPacket);

			int serverPort = getServerPort(receivedPacket);

			//Form new packet from received packet
			System.out.println("\nError Simulator: Forming new Packet:");
			DatagramPacket newPacket = new DatagramPacket(receivedPacket.getData(), receivedPacket.getData().length, address, serverPort);
			Resources.printPacketInformation(newPacket);

			//Send the newly formed packet to server
			System.out.println("\nError Simulator: Sending packet to server:");
			Resources.printPacketInformation(newPacket);
			Resources.sendPacket(newPacket, serverSocket);
			System.out.println("Error Simulator: Packet sent to server!\n");
			port = receivedPacket.getPort();
		} 
		catch (SocketTimeoutException e) {
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
		if(request == Request.ACK || request == Request.DATA) {
			if (connections.containsKey(clientPort)) {
				return connections.get(clientPort);
			}
		}

		connections.put(clientPort, -1);
		return Resources.serverPort;
	}

	/**
	 * Forward the client packet to the server
	 */
	private void forwardServerPacket(int clientPort)
	{
		//Receive response packet from the server
		System.out.println("Error Simulator: Waiting for packet from server\n");
		DatagramPacket receivedServerPacket;
		try {
			receivedServerPacket = Resources.receivePacket(serverSocket);
			System.out.println("Error Simulator: Packet received from server:");
			Resources.printPacketInformation(receivedServerPacket);

			// save the port the server port
			this.connections.put(clientPort,receivedServerPacket.getPort());

			//Create new packet to send to client
			DatagramPacket sendPacket = new DatagramPacket(receivedServerPacket.getData(), receivedServerPacket.getData().length, address, clientPort);
			System.out.println("\nIntermediate Host: Sending packet to client");
			Resources.printPacketInformation(sendPacket);
			Resources.sendPacket(sendPacket, clientSocket);
			System.out.println("\nError Simulator: Packet sent to client!\n");
		} catch (SocketTimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Execute the Error Simulator
	 * @param args Arguments
	 */
	public static void main(String[] args) {
		ErrorSimulator es = new ErrorSimulator();
		//Listen forever...
		while(true) {
			es.sendAndReceive();
		}
	}

}