package cis.host;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Scanner;
import cis.utils.Request;
import cis.utils.Resources;

public class Operation {

	public Mode mode;
	public Request type;
	public int packetNumber;
	public int delay;
	private boolean hasErrorOccured;

	public Operation(Scanner sn) {
		this.hasErrorOccured = false;
		this.mode = getMode(sn);
		if (mode != Mode.NORMAL) {
			this.type = getRequestType(sn);
			if (type == Request.ACK || type == Request.DATA) {
				System.out.println("Please enter the block number of the packet");
				this.packetNumber = getNumber(sn);
			}
			if (mode == Mode.DELAY) {
				System.out.println("Please enter the time for the delay in milliseconds.");
				this.delay = getNumber(sn);
			}
		}
	}

	/**
	 * Prompts the user for a number
	 */
	private int getNumber(Scanner inputScanner) {
		while (true) {
			String num = inputScanner.nextLine().toUpperCase();
			try {
				return Integer.parseInt(num);
			} catch (NumberFormatException e) {
				System.out.println("Not a valid number. Please try again.");
			}
		}
	}

	/**
	 * Prompts the user for the type of Request it wants
	 * 
	 * @return the request type
	 */
	private Request getRequestType(Scanner inputScanner) {
		System.out.println("Please Enter the type of packet. D for Data and A for ACK: ");

		while (true) {
			String type = inputScanner.nextLine().toUpperCase();
			if (type.equals(Request.DATA.getType())) {
				return Request.DATA;
			} else if (type.equals(Request.ACK.getType())) {
				return Request.ACK;
			} else {
				System.out.println("Not a valid request request.");
			}
		}
	}

	/**
	 * Prompts for the operation type
	 * 
	 * @return the request type
	 */
	private Mode getMode(Scanner inputScanner) {
		System.out.println("Please Enter Operation Mode."
				+ " Enter 0 for normal operation; 1 to lose a packet; 2 to delay a packet; 3 to duplicate a packet; 4 for invalid opcode; 5 for unkown transfer id.");

		while (true) {
			String mode = inputScanner.nextLine().toUpperCase();
			if (mode.equals("0")) {
				return Mode.NORMAL;
			} else if (mode.equals("1")) {
				return Mode.LOSE;
			} else if (mode.equals("2")) {
				return Mode.DELAY;
			} else if (mode.equals("3")) {
				return Mode.DUPLICATE;
			} else if (mode.equals("4")) {
				return Mode.ILLEGAL;
			} else if (mode.equals("5")) {
				return Mode.UNKOWN_TRANSFER;
			} else {
				System.out.println("Not a valid mode.");
			}
		}
	}

	public Mode sendPacket(DatagramPacket packet, DatagramSocket socket) {

		int blockNumber = Resources.getBlockNumber(packet.getData());
		Resources.printPacketInformation(packet);
		if (this.mode == Mode.NORMAL) {
			Resources.sendPacket(packet, socket);
			System.out.println("Packet sent.");
		} else if (isNetworkErrorPacket(blockNumber, Resources.packetRequestType(packet))) {
			this.hasErrorOccured = true;
			switch (this.mode) {
			case DELAY:
				System.out.println("Packet " + blockNumber + " will be delayed by " + this.delay + " milliseconds");

				new java.util.Timer(true).schedule(new java.util.TimerTask() {
					@Override
					public void run() {
						Resources.sendPacket(packet, socket);
						System.out.println("Sent delayed Packet");
					}
				}, delay);

				return Mode.DELAY;
			case ILLEGAL:
				System.out.println("Packet " + blockNumber + " will have a illegal opcode");
				byte[] data = new byte[4];
				data[0] = 0;
				data[1] = -23;
				DatagramPacket illegalPacket = new DatagramPacket(data, data.length, packet.getAddress(),
						packet.getPort());
				Resources.sendPacket(illegalPacket, socket);
				System.out.println("Packet sent.");
				return Mode.ILLEGAL;
			case LOSE:
				System.out.println("Packet " + blockNumber + " will be dropped");
				return Mode.LOSE;
			case DUPLICATE:
				System.out.println("Packet " + blockNumber + " will be duplicated");
				Resources.sendPacket(packet, socket);
				Resources.sendPacket(packet, socket);
				return Mode.DUPLICATE;
			case UNKOWN_TRANSFER:
				System.out.println("Packet " + blockNumber + " will be sent with an unkown transfer code.");
				this.sendPacketWithUnkownTransferID(packet);
				return Mode.UNKOWN_TRANSFER;
			}
		} else {
			Resources.sendPacket(packet, socket);
			System.out.println("Packet sent.");
		}

		return Mode.NORMAL;
	}
	
	private void sendPacketWithUnkownTransferID(DatagramPacket packet)
	{
		try {
			DatagramSocket socket = new DatagramSocket();
			Resources.sendPacket(packet, socket);
			socket.close();
		} catch (SocketException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	private boolean isNetworkErrorPacket(int blockNumber, Request requestType) {

		if (requestType == this.type && !this.hasErrorOccured) {
			return (this.packetNumber == blockNumber);
		}

		return false;
	}

	public enum Mode {
		NORMAL, LOSE, DELAY, DUPLICATE, ILLEGAL, UNKOWN_TRANSFER
	}
}
