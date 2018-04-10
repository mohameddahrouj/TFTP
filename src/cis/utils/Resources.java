package cis.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.Formatter;

/**
 * Common class between Client, ErrorSimulator and Server to print, send and
 * receive packets Last edited January 30th, 2018
 * 
 * @author Mohamed Dahrouj, Ali Farah, Lava Tahir, Tosin Oni, Vanja Veselinovic
 *
 */
public class Resources {
	private static final String octet = "octet";

	// Shared port between client and intermediate host
	public static final int errorSimulatorPort = 23;

	// Shared port between intermediate host and server
	public static final int serverPort = 69;

	// 10s timeout
	public static final int timeout = 300000;

	public static final int CLIENT = 1;
	public static final int SERVER = 2;

	/**
	 * Print packet information as string or bytes
	 * 
	 * @param packet
	 *            Packet to be printed as string or byte representation
	 */
	public static void printPacketInformation(DatagramPacket packet) {
		// System.out.println("From host: " + packet.getAddress());
		// System.out.println("Host port: " + packet.getPort());
		// System.out.println("Length: " + packet.getLength());

		// Print byte array
		Formatter f = new Formatter();
		for (byte b : packet.getData()) {
			f.format("%02x ", b);
		}
		System.out.println("Containing Bytes: " + f.toString());
		// Close the formatter once all bytes are outputted
		f.close();
	}

	/**
	 * Send packet to specified socket
	 * 
	 * @param packet
	 *            Packet
	 * @param socket
	 *            Socket
	 */
	public static void sendPacket(DatagramPacket packet, DatagramSocket socket) {
		try {
			socket.send(packet);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Receive the packet from specified Socket
	 * 
	 * @param socket
	 *            Socket
	 * @return Received datagram packet
	 */
	public static DatagramPacket receivePacket(DatagramSocket socket) throws IOException {
		// Construct a DatagramPacket for receiving packets
		byte data[] = new byte[516];

		DatagramPacket receivedPacket = new DatagramPacket(data, data.length);

		// Block until a datagram is received via the socket
		socket.receive(receivedPacket);	

		receivedPacket.setData(truncateData(receivedPacket));
		receivedPacket.setLength(receivedPacket.getData().length);
		
		return receivedPacket;
	}

	/**
	 * Remove trailing bytes from a byte array.
	 * 
	 * @param data
	 *            The data
	 * @return Data with trailing FF bytes removed
	 */
	public static byte[] truncateData(DatagramPacket packet) {
		byte[] data = new byte[packet.getLength()];
		System.arraycopy(packet.getData(), packet.getOffset(), data, 0, packet.getLength());

		return data;
	}

	public static int getBlockNumber(byte[] data) {
		return ((data[2] & 0xff) << 8) | (data[3] & 0xff);
	}

	/**
	 * Determine if packet is a read or write request
	 * 
	 * @param packet
	 *            Packet
	 * @return Request type of packet
	 */
	public static Request packetRequestType(DatagramPacket packet) {
		// The second element of a read request should be 1
		// Whereas the second element of a write request should be 2
		if (packet.getData()[1] == (byte) 0x01) {
			return Request.READ;
		} else if (packet.getData()[1] == (byte) 0x02) {
			return Request.WRITE;
		} else if (packet.getData()[1] == (byte) 0x03) {
			return Request.DATA;
		} else if (packet.getData()[1] == (byte) 0x04) {
			return Request.ACK;
		} else if (packet.getData()[1] == (byte) 0x05) {
			return Request.ERROR;
		}
		return Request.INVALID;
	}

	public static boolean doesFileExist(String filePath) {
		File file = new File(filePath);

		return file.exists() && !file.isDirectory();
	}

	/**
	 * Create request format as per specification
	 */
	public static byte[] createRequest(Request request, String filePath) {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		try {
			byteArrayOutputStream.write(request.getBytes());
			byteArrayOutputStream.write(filePath.getBytes());
			byteArrayOutputStream.write(0);
			byteArrayOutputStream.write(octet.getBytes());
			byteArrayOutputStream.write(0);
		} catch (IOException exception) {
			exception.printStackTrace();
			System.exit(1);
		}

		return byteArrayOutputStream.toByteArray();
	}
}
