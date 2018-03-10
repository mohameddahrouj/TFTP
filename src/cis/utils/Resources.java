package cis.utils;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.Formatter;

/**
 * Common class between Client, ErrorSimulator and Server to print, send and receive packets
 * Last edited January 30th, 2018
 * @author Mohamed Dahrouj, Ali Farah, Lava Tahir, Tosin Oni, Vanja Veselinovic
 *
 */
public class Resources {
	//Shared port between client and intermediate host
	public static final int clientPort = 23;
	
	//Shared port between intermediate host and server
	public static final int serverPort = 69;

	//10s timeout
	public static final int timeout = 300000;

	
	public static final int CLIENT = 1;
	public static final int SERVER = 2;
	
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
	public static DatagramPacket receivePacket(DatagramSocket socket) throws SocketTimeoutException{
		// Construct a DatagramPacket for receiving packets
		byte data[] = new byte[516];

		// Initialize the array to contain all FF bytes. This way, when
		// the data is filled in we can truncate all trailing FFs, since
		// we know a valid request must end with a 0 byte. Otherwise,
		// all packets would be 100 bytes long.
		java.util.Arrays.fill(data, (byte) 0xFF);

		DatagramPacket receivePacket = new DatagramPacket(data, data.length);
	
	    try {
			// Block until a datagram is received via the socket
			socket.receive(receivePacket);
		}
	    catch(IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		// Truncate to remove trailing FFs so that we only get the intended
	    // data and its length.
	    receivePacket.setData(truncateData(data));
	    receivePacket.setLength(receivePacket.getData().length);
		
		return receivePacket;
	}

	/**
	 * Remove trailing FF bytes from a byte array.
	 * @param data The data
	 * @return Data with trailing FF bytes removed
	 */
	public static byte[] truncateData(byte[] data) {
		int endIndex = 0;
		int i = data.length - 1;
		
		while (i >= 0 && data[i] == (byte) 0xFF) {
			i--;
		}
		
		if (i < 0) return new byte[] {};
		
		endIndex = i;
		byte[] truncatedData = new byte[endIndex+1];
		
		for (i = 0; i <= endIndex; i++) {
			truncatedData[i] = data[i];
		}
		
		return truncatedData;
	}

	/**
	 * Determine if packet is a read or write request
	 * @param packet Packet
	 * @return Request type of packet
	 */
	public static Request packetRequestType(DatagramPacket packet) {
		//The second element of a read request should be 1
		//Whereas the second element of a write request should be 2
		if(packet.getData()[1] == (byte) 0x01) {
			return Request.READ;
		}
		else if(packet.getData()[1] == (byte) 0x02) {
			return Request.WRITE;
		}
		else if(packet.getData()[1] == (byte) 0x03) {
			return Request.DATA;
		}
		else if(packet.getData()[1] == (byte) 0x04) {
			return Request.ACK;
		}
		else if(packet.getData()[1] == (byte) 0x05) {
			return Request.ERROR;
		}
		return Request.INVALID;
	}
	
    public static boolean doesFileExist(String filePath)
    {
    	File file = new File(filePath);
    	
    	return file.exists() && !file.isDirectory();
    }
}
