package cis.client;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

import cis.handlers.ReadHandler;
import cis.handlers.WriteHandler;
import cis.utils.Request;
import cis.utils.Resources;

/**
 * This class is the client side for a server based on UDP/IP.
 * The client communicates with the error simulator at the specified port.
 * Last edited January 30th, 2018
 * @author Mohamed Dahrouj, Ali Farah, Lava Tahir, Tosin Oni, Vanja Veselinovic
 *
 */
public class Client {
	private DatagramSocket socket;

	private final String octet = "ocTEt";
	private byte[] mode;
	private String fileName;
	private Request request;

	private InetAddress address;
	
	public Client() {
		try {
			// Construct a datagram socket and bind it to any available
	        // port on the local host machine. This socket will be used to
	        // send and receive UDP Datagram packets.
			socket = new DatagramSocket();
			//Initialize octet bytes
			mode = octet.getBytes();
			
			//Initialize address
			address = InetAddress.getLocalHost();
            this.request = getRequestType();
			this.fileName = getFilePath();
		}
		catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Read request format as per specification
	 */
	private byte[] createRequest(){
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		try {
			byteArrayOutputStream.write(this.request.getBytes());
			byteArrayOutputStream.write(this.fileName.getBytes());
			byteArrayOutputStream.write(0);
			byteArrayOutputStream.write(mode);
			byteArrayOutputStream.write(0);
		}
		catch (IOException exception)
		{
			exception.printStackTrace();
			System.exit(1);
		}

		return byteArrayOutputStream.toByteArray();
	}

	/**
	 * Sends the initial read/write request to error simulator
	 */
	private void sendRequest()
	{
		byte[] request = createRequest();

		//Send the packet given request, address and port
		DatagramPacket packet = new DatagramPacket(request, request.length, address, Resources.clientPort);
		System.out.println("Client: Sending packet to intermediate host:");
		Resources.printPacketInformation(packet);
		Resources.sendPacket(packet, socket);
		System.out.println("Client: Packet sent!\n");
	}
	
	/**
	 * Process the requests then send and receive
	 */
	public void processRequest() {

		this.sendRequest();

		if (this.request == Request.READ) {
			ReadHandler readHandler = new ReadHandler(this.socket, address, Resources.clientPort, "temp.txt");
			readHandler.process();


		} else if (this.request == Request.WRITE) {

			WriteHandler writeHandler = new WriteHandler(this.socket, address, Resources.clientPort,"temp.txt");
			writeHandler.waitForACK();
			writeHandler.process();
		}
	}

	/**
	 * Prompts the user for the path of the file it wants to read/write to
	 * @return the path of the file
	 */
	private String getFilePath() {
		String path = "";
		System.out.println("Please Enter file path: ");

		Scanner scanner = new Scanner(System.in);
		while (path.isEmpty()) {
			path = scanner.nextLine();
			if (path.isEmpty())
				System.out.print("Not a valid file path. Please renter file path");
		}

		scanner.close();
		return path;
	}

	/**
	 * Prompts the user for the type of Request it wants
	 * @return the request type
	 */
	private Request getRequestType()
	{
		System.out.println("Please Enter Request request. R for Read and W for Write: ");
		Scanner scanner = new Scanner(System.in);

		boolean isRequestObtained = true;
		Request selectedRequest = null;
		
		while(isRequestObtained)
		{
			String type = scanner.nextLine().toUpperCase();
			if(type.equals(Request.READ.getType()))
			{
				selectedRequest = Request.READ;
				isRequestObtained = false;
			}
			else if(type.equals(Request.WRITE.getType()))
			{
				selectedRequest = Request.WRITE;
				isRequestObtained = false;
			}
			else
			{
				System.out.println("Not a valid request request.Type R for Read and W for Write");
			}
		}
		
		scanner.close();
		return selectedRequest;
	}

	/**
	 * Execute the client to send and receive requests to the port
	 * @param args Arguments
	 */
	public static void main(String[] args) {
		Client client = new Client();
		client.processRequest();
	}

}