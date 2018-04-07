package cis.handlers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Authenticator.RequestorType;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cis.utils.IOErrorType;
import cis.utils.Request;
import cis.utils.Resources;

/**
 * This class will receive data from a file and write it to a local file.
 * 
 * @author Mohamed Dahrouj, Ali Farah, Lava Tahir, Tosin Oni, Vanja Veselinovic
 */
public class ReceiverHandler extends Handler {

	private static final byte prefixNumber = 4;
	private final static int maxBlockLength = 516;
	private FileOutputStream fileStream;
	private int recievedBlocks;

	public ReceiverHandler(DatagramSocket socket, InetAddress address, int port, String filePath, String directory,
			int requester) {
		super(socket, prefixNumber, address, port, filePath, requester);
		this.fileStream = this.createFile(directory);
		this.recievedBlocks = 0;
	}

	@Override
	public void process() {

		boolean isFinalPacket = false;

		if (this.requester == Resources.SERVER) {
			this.sendAck(0);
		}

		while (!isFinalPacket) {

			try {
				DatagramPacket receivedPacket = waitForData();
				// send Ack for data received
				int blockNumber = Resources.getBlockNumber(receivedPacket.getData());
				writeToFile(blockNumber,receivedPacket.getData());
				sendAck(recievedBlocks);
				isFinalPacket = isFinalPacket(receivedPacket);
			} catch (SocketTimeoutException e) {
				System.out.println("Timeout has occured on the Reciever side.");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			this.fileStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private DatagramPacket waitForData() throws IOException
	{
		System.out.println("Waiting to get Data");
		DatagramPacket receivedPacket = Resources.receivePacket(this.sendAndReceiveSocket);
		System.out.println("Data Received: ");
		Resources.printPacketInformation(receivedPacket);		
		Request type = Resources.packetRequestType(receivedPacket);	
		processPacket(receivedPacket);
		
		if (type == Request.ERROR) {
			IOErrorType error = super.getErrorType(receivedPacket.getData());
			System.out.println("Recieved an error packet. Error is of type " + error.getErrorMessage());
			if(error == IOErrorType.UnkownTransferID)
			{
				System.out.println("Resending the previous packet");
				sendAck(this.recievedBlocks);
				return this.waitForData();
			}
			else
			{
				System.out.println("Closing the connection and exiting");
				System.exit(1);
			}
		}
		else if(type == Request.WRITE)
		{
			System.out.println("Recieved a write Request. Resending the initial ack");
			this.sendAck(0);
			return this.waitForData();
		}
		else if(receivedPacket.getPort() != this.port)
		{
			System.out.println("Recieved a packet with an unkown transfer id. Sending error packet");
			this.sendErrorPacket(IOErrorType.UnkownTransferID);
			return this.waitForData();
		}
		
		return receivedPacket;
	}

	public void sendAck(int blockNumber) {
		byte[] ack = super.getPrefix(blockNumber);
		DatagramPacket packet = new DatagramPacket(ack, ack.length, address, port);
		System.out.println("Sending Ack for block: " + blockNumber);
		Resources.printPacketInformation(packet);
		Resources.sendPacket(packet, this.sendAndReceiveSocket);
		System.out.println("Ack sent\n");
	}

	private FileOutputStream createFile(String directory) {
		String fileName = Paths.get(this.filePath).getFileName().toString();
		String newFilePath = "./" + directory + "/" + fileName;
		try {
			File file = new File(newFilePath);
			file.getParentFile().mkdirs();
			if (file.createNewFile()) {
				return new FileOutputStream(file);
			} else {
				// file already exists send error packet and return
				System.out.println(newFilePath + " already exists! Sending File exists packet.");
				super.sendErrorPacket(IOErrorType.FileExists);
				System.exit(1);
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}

	/**
	 * Write the data received to a local file.
	 */
	private void writeToFile(int blockNumber, byte[] data) {
		
		if(blockNumber <= this.recievedBlocks)
		{
			// if received data block is less then or equal to the most recent block then we
			// already received this packet
			System.out.println("Recieved Duplicate data packet. Data will not be written to the file.");
			return;
		}

		this.recievedBlocks = blockNumber;
		try {
			fileStream.write(Arrays.copyOfRange(data, 4, data.length));
		} catch (IOException exception) {
			System.out.println("Error copying data to file");
			super.sendErrorPacket(IOErrorType.DiskFull);
			exception.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Checks if the packet is the if final packet
	 * 
	 * @param receivedPacket
	 *            The packet received from the sender.
	 * @return
	 */
	private boolean isFinalPacket(DatagramPacket receivedPacket) {
		return receivedPacket.getLength()< maxBlockLength;
	}
}
