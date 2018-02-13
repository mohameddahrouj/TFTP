package cis.client;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

import cis.handlers.ReceiverHandler;
import cis.handlers.SenderHandler;
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
	private Scanner inputScanner;
	
	public Client() {
		try {
			// Construct a datagram socket and bind it to any available
	        // port on the local host machine. This socket will be used to
	        // send and receive UDP Datagram packets.
			socket = new DatagramSocket();
			inputScanner = new Scanner(System.in);
			//Initialize octet bytes
			mode = octet.getBytes();
			
			//Initialize address
			address = InetAddress.getLocalHost();
            this.request = getRequestType();
			this.fileName = getFilePath();
			inputScanner.close();
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
			ReceiverHandler receiverHandler = new ReceiverHandler(this.socket, address, Resources.clientPort, "temp.txt", "Client");
			receiverHandler.process();


		} else if (this.request == Request.WRITE) {

			SenderHandler senderHandler = new SenderHandler(this.socket, address, Resources.clientPort,"temp.txt");
			senderHandler.waitForACK();
			senderHandler.process();
		}
	}

	/**
	 * Prompts the user for the path of the file it wants to read/write to
	 * @return the path of the file
	 */
	private String getFilePath() {
		String path = "";
		System.out.println("Please Enter file path: ");

		while (path.isEmpty()) {
			path = inputScanner.nextLine();
			if (path.isEmpty())
				System.out.print("Not a valid file path. Please renter file path: ");
		}

		return path;
	}

	/**
	 * Prompts the user for the type of Request it wants
	 * @return the request type
	 */
	private Request getRequestType()
	{
		System.out.println("Please Enter Request request. R for Read and W for Write: ");
		
		while(true)
		{
			String type = inputScanner.nextLine().toUpperCase();
			if(type.equals(Request.READ.getType()))
			{
				return Request.READ;
			}
			else if(type.equals(Request.WRITE.getType()))
			{
				return Request.WRITE;
			}
			else
			{
				System.out.println("Not a valid request request.Type R for Read and W for Write");
			}
			
		}		
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