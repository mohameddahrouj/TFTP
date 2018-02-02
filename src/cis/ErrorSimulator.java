package cis;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Error Simulator is the intermediary between client and server
 * Last edited January 30th, 2018
 * @author Mohamed Dahrouj, Lava Tahir
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
		//Receive request from client
		System.out.println("Waiting to receive a request from client...");
		DatagramPacket receivedPacket = Resources.receivePacket(clientSocket);	
		//Process the received packet from client socket
		System.out.println("Error Simulator: Packet received:");
		Resources.printPacketInformation(receivedPacket);
		int serverPort = Resources.serverPort;

		if(connections.containsKey(receivedPacket.getPort()))
			serverPort = connections.get(receivedPacket.getPort());
		else
		{
			connections.put(receivedPacket.getPort(),-1);
		}

		//Form new packet from received packet
		System.out.println("\nError Simulator: Forming new Packet:");
		DatagramPacket newPacket = new DatagramPacket(receivedPacket.getData(), receivedPacket.getData().length, address, serverPort);
		Resources.printPacketInformation(newPacket);
		
		//Send the newly formed packet to server
		System.out.println("\nError Simulator: Sending packet to server:");
		Resources.printPacketInformation(newPacket);
		Resources.sendPacket(newPacket, serverSocket);
		System.out.println("Error Simulator: Packet sent to server!\n");
		
		//Receive response packet from the server
		System.out.println("Error Simulator: Waiting for packet from server\n");
		DatagramPacket receivedServerPacket = Resources.receivePacket(serverSocket);
		System.out.println("Error Simulator: Packet received from server:");
		Resources.printPacketInformation(receivedServerPacket);
		connections.put(receivedPacket.getPort(),receivedServerPacket.getPort());
		//Create new packet to send to client 
		DatagramPacket sendPacket = new DatagramPacket(receivedServerPacket.getData(), receivedServerPacket.getData().length, address, receivedPacket.getPort());
		System.out.println("\nIntermediate Host: Sending packet to client");
		Resources.printPacketInformation(sendPacket);
		Resources.sendPacket(sendPacket, clientSocket);
		System.out.println("\nError Simulator: Packet sent to client!\n");
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