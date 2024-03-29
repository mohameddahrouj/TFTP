package cis.handlers;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;

import cis.utils.IOErrorType;
import cis.utils.Request;
import cis.utils.Resources;

/**
 * This class will send the contents of a file to the receiver.
 * 
 * @author Mohamed Dahrouj, Ali Farah, Lava Tahir, Tosin Oni, Vanja Veselinovic
 *
 */
public class SenderHandler extends Handler {

	private static final byte prefixNumber = 3;
	private static final int maxBlockSize = 512;

	private int blockNumber;
	private byte[] fileData;
	private int ackBlockNumber;

	public SenderHandler(DatagramSocket socket, InetAddress address, int port, String filePath, int requester) {
		super(socket, prefixNumber, address, port, filePath, requester);
		this.blockNumber = 0;
		if (requester == Resources.CLIENT) {
			this.ackBlockNumber = 0; // the first ACK will have a number of 0 if the client is sending .
		} else {
			this.ackBlockNumber = 1;
		}
		this.fileData = readFileAndConvertToByteArray(this.filePath);
	}

	/**
	 *
	 * @return return whether or not this is the final packet
	 */
	private boolean isFinalPacket() {
		return this.blockNumber * maxBlockSize > this.fileData.length;
	}

	@Override
	public void process() {
		boolean isFinalPacket = false;

		if (this.requester == Resources.CLIENT) {
			waitForInitialACK();
		}

		while (!isFinalPacket) {
			this.sendData();
			try {
				this.waitForACK();
			} catch (SocketTimeoutException e) {
				this.blockNumber--;
				System.out.println("Timeout has occurred. Resending block number " + (this.blockNumber+1));
			} catch (IOException e) {
				e.printStackTrace();
			}
			isFinalPacket = isFinalPacket();
		}
		System.out.println("Last Packet sent and ack was recieved. Exiting");
	}
	
	private void waitForInitialACK()
	{
		try {
			this.waitForACK();
		} catch (SocketTimeoutException e) {
			System.out.println("Timeout has occured resending the write request");
			this.sendWriteRequest();
			this.waitForInitialACK();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This will a block of data to the receiver
	 */
	private void sendData() {
		System.out.println("Sending Block: " + (this.blockNumber+1));
		DatagramPacket packet = createWritePacket();
		Resources.printPacketInformation(packet);
		Resources.sendPacket(packet, this.sendAndReceiveSocket);
		System.out.println("Data sent\n");
		this.blockNumber++; // increase the blockNumber after each write
	}
	
	private void sendWriteRequest()
	{
		byte[] request = Resources.createRequest(Request.WRITE, this.filePath);
		DatagramPacket packet =  new DatagramPacket(request, request.length, address, port);
		Resources.sendPacket(packet, this.sendAndReceiveSocket);
		System.out.println("Data sent\n");
	}

	/**
	 * Waits of the receiver to send an ACK
	 */
	public void waitForACK() throws IOException {
		System.out.println("Wait to Receive ACK");
		// wait for ack to start the writing process
		DatagramPacket receivedPacket = Resources.receivePacket(this.sendAndReceiveSocket);
		System.out.println("ACK Received:");
		Resources.printPacketInformation(receivedPacket);
		int receivedAck = Resources.getBlockNumber(receivedPacket.getData());
		Request type = Resources.packetRequestType(receivedPacket);	
		processPacket(receivedPacket);

		if (type == Request.ERROR) {
			IOErrorType error = super.getErrorType(receivedPacket.getData());
			System.out.println("Recieved an error packet. Error is of type " + error.getErrorMessage());
			if(error == IOErrorType.UnkownTransferID)
			{
				System.out.println("Resending the previous packet");
				this.blockNumber--;
				this.sendData();
				this.waitForACK();
				return;
			}
			else
			{
				System.out.println("Closing the connection and exiting");
				System.exit(1);
			}
		}
		
		else if (receivedAck != this.ackBlockNumber) {
			System.out.println(
					"Recieved Delayed/Duplicate ACK Packet.Expected " + this.ackBlockNumber + " recieved is " + receivedAck);
			this.waitForACK();
			return;
		}
		else if(receivedPacket.getPort() != this.port)
		{
			System.out.println("Recieved a packet with an unkown transfer id. Sending error packet");
			this.sendErrorPacket(IOErrorType.UnkownTransferID);
			this.waitForACK();
			return;
		}

		this.ackBlockNumber++;
	}

	/**
	 * Converts a file to a byte array
	 * 
	 * @param filename
	 *            the name of the file you want to convert to bytes
	 * @return the byte array of the file
	 */
	private byte[] readFileAndConvertToByteArray(String filename) {
		File file = new File(filename);
		byte[] fileBytes = new byte[(int) file.length()];

		try {
			FileInputStream fis = new FileInputStream(file);
			fis.read(fileBytes); // read file into bytes[]
			fis.close();
		} catch (FileNotFoundException ex) {
			System.out.println("Could not find " + filename + " Sending file not found error packet.");
			this.sendErrorPacket(IOErrorType.FileNotFound);
			System.exit(1);

		} catch (SecurityException ex) {
			System.out.println(filename + " Packet is readonly. Sending access violation error packet.");
			this.sendErrorPacket(IOErrorType.AccessViolation);
			System.exit(1);
		} catch (IOException ex) {
			System.out.println("IO Exception when reading '" + filename + "'");
			System.exit(1);

		}
		return fileBytes;
	}

	/**
	 * Creates the packet with the file data.
	 * 
	 * @return a DatagramPacket
	 */
	private DatagramPacket createWritePacket() {
		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			outputStream.write(super.getPrefix(blockNumber + 1));
			outputStream.write(getFileData());
			byte[] data = outputStream.toByteArray();
			DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
			return packet;
		} catch (IOException ex) {
			System.out.println("IO Exception when reading creating write packet");
			System.exit(1);
		}

		return null;
	}

	/**
	 * Gets one block of data from the file.
	 * 
	 * @return One block of the file
	 */
	private byte[] getFileData() {
		int start = this.blockNumber * maxBlockSize;
		int end = start + maxBlockSize;
		if(start >= this.fileData.length)
		{
			return new byte[] {};
		}

		if (end > this.fileData.length) {
			end = this.fileData.length;
		}

		return Arrays.copyOfRange(this.fileData, start, end);
	}

}
