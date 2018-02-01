package cis;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

/**
 * This class is the client side for a server based on UDP/IP.
 * The client communicates with the error simulator at the specified port.
 * Last edited January 30th, 2018
 * @author Mohamed Dahrouj, Lava Tahir
 *
 */
public class Client implements Runnable{
	private DatagramSocket socket;

	private final String octet = "ocTEt";
	private byte[] mode;
	private String fileName;
	private Request type;

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
			this.fileName = getFilename();
			this.type = getRequestType();
		}
		catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Read request format as per specification
	 * @throws IOException
	 */
	private byte[] createRequest(){
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

		try {
			byteArrayOutputStream.write(this.type.getBytes());
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
	 * Process the requests then send and receive
	 */
	private void processRequest() {
		byte[] request = createRequest();
		
		//Send the packet given request, address and port
		DatagramPacket packet = new DatagramPacket(request, request.length, address, Resources.clientPort);
		System.out.println("Client: Sending packet to intermediate host:");
		Resources.printPacketInformation(packet);
		Resources.sendPacket(packet, socket);
		System.out.println("Client: Packet sent!\n");

	}




	private String getFilename() {
		String path = "";
		System.out.println("Please Enter file name: ");

		while (path.isEmpty()) {
			Scanner scanner = new Scanner(System.in);
			path = scanner.nextLine();
			if (path.isEmpty())
				System.out.print("Not a valid file name. Please renter file name");
		}

		return path;
	}

	private Request getRequestType()
	{
		System.out.println("Please Enter Request type. R for Read and W for Write: ");
		while(true)
		{
			Scanner scanner = new Scanner(System.in);
			String type = scanner.nextLine();
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
				System.out.println("Not a valid request type.Type R for Read and W for Write");
			}

		}
	}


	@Override
	public void run(){
		this.processRequest();
	}
	/**
	 * Execute the client to send and receive requests to the port
	 * @param args Arguments
	 * @throws InterruptedException
	 */
	public static void main(String[] args) {
		/**
		 * Send 5 read, 5 write and 1 invalid requests to intermediate host.
		 * And receive
		 */
		for (int counter = 1; counter<= 10; counter++) {

			Thread readReqThread = new Thread(new Client());
			readReqThread.start();
			try {
				Thread.sleep(2500);
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}

	}

}