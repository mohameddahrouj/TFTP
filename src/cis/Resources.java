package cis;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.Formatter;

/**
 * Common class between Client, ErrorSimulator and Server to print, send and receive packets
 * Last edited January 16th, 2018 
 * @author Mohamed Dahrouj
 *
 */
public class Resources {
	//Shared port between client and intermediate host
	public static final int clientPort = 23;
	
	//Shared port between client and intermediate host
	public static final int serverPort = 69;
	
	/**
	 * Print packet information as string or bytes
	 * @param packet Packet to be printed as string or byte representation
	 */
	public static void printPacketInformation(DatagramPacket packet) {
		System.out.println("From host: " + packet.getAddress());
		System.out.println("Host port: " + packet.getPort());
		System.out.println("Length: " + packet.getLength());
		
		// Form a String from the byte array.
		String received = new String(packet.getData(),0,packet.getLength());
		System.out.println("Containing String: " + received);
		
		//Print byte array
		Formatter f = new Formatter();
		for(byte b : packet.getData()) {
			f.format("%02x ", b);
		}
		System.out.println("Containing Bytes: " + f.toString());
		//Close the formatter once all bytes are outputted
		f.close();	
	}

	/**
	 * Send packet to specified socket
	 * @param packet Packet
	 * @param socket Socket
	 */
	public static void sendPacket(DatagramPacket packet, DatagramSocket socket){
		try {
			socket.send(packet);
		}
		catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	/**
	 * Receive the packet from specified Socket
	 * @param socket Socket
	 * @return Received datagram packet
	 */
	public static DatagramPacket receivePacket(DatagramSocket socket){
		// Construct a DatagramPacket for receiving packets
		byte data[] = new byte[100];
		DatagramPacket receivePacket = new DatagramPacket(data, data.length);
	
	    try {
			// Block until a datagram is received via the socket
			socket.receive(receivePacket);
		}
		catch(SocketTimeoutException e){
	    	System.out.println("Socket has timed out. System will exit.");
	    	System.exit(1);
		}
	    catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return receivePacket;
	}
}
