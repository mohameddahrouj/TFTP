package cis.host;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import cis.utils.Request;
import cis.utils.Resources;

public class Operation {

	public Mode mode;
	public Request type;
	public int packetNumber;
	public int delay;

	public Operation() {
		Scanner sn = new Scanner(System.in);
		this.mode = getMode(sn);
		if (mode != Mode.NORMAL) {
			this.type = getRequestType(sn);
			if (type == Request.ACK || type == Request.DATA) {
				System.out.println("Please enter the block number of the packet");
				this.packetNumber = getNumber(sn);
			}
			if (mode == Mode.DELAY) {
				System.out.println("Please enter the time for the delay in seconds.");
				this.delay = getNumber(sn);
			}
		}

		sn.close();
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
		System.out.println("Please Enter the type of packet. R for Read, W for Write, D for Data and A for ACK: ");

		while (true) {
			String type = inputScanner.nextLine().toUpperCase();
			if (type.equals(Request.READ.getType())) {
				return Request.READ;
			} else if (type.equals(Request.WRITE.getType())) {
				return Request.WRITE;
			} else if (type.equals(Request.DATA.getType())) {
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
				+ " Enter 0 for normal operation; 1 to lose a packet; 2 to delay a packet; 3 to duplicate a packet ");

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
			} else {
				System.out.println("Not a valid mode.");
			}
		}
	}

	public void sendPacket(DatagramPacket packet, DatagramSocket socket) {

		int blockNumber = Resources.getBlockNumber(packet.getData());
		Resources.printPacketInformation(packet);
		if (this.mode == Mode.NORMAL) {
			Resources.sendPacket(packet, socket);
			System.out.println("Packet sent.");
		} else if (isNetworkErrorPacket(blockNumber, Resources.packetRequestType(packet))) {
			switch (this.mode) {
			case DELAY:
				System.out.println("Packet " + blockNumber + " will be delayed by " + this.delay + " seconds");
				delaySendingPacket(packet, socket);
				break;
			case LOSE:
				System.out.println("Packet " + blockNumber + " will be dropped");
				break;
			case DUPLICATE:
				System.out.println("Packet " + blockNumber + " will be duplicated");
				Resources.sendPacket(packet, socket);
				Resources.sendPacket(packet, socket);
				break;
			}
		}
		else
		{
			Resources.sendPacket(packet, socket);
			System.out.println("Packet sent.");
		}
	}

	private void delaySendingPacket(DatagramPacket packet, DatagramSocket socket) {
		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

		Runnable task = new Runnable() {
			public void run() {
				Resources.sendPacket(packet, socket);
				System.out.println("Sent delayed Packet");
			}
		};

		scheduler.schedule(task, this.delay, TimeUnit.SECONDS);
		scheduler.shutdown();
	}

	private boolean isNetworkErrorPacket(int blockNumber, Request requestType) {

		if (requestType == this.type && (this.type == Request.READ || this.type == Request.WRITE)) {
			return true;
		}

		return requestType == this.type && this.packetNumber == blockNumber;
	}

	public enum Mode {
		NORMAL, LOSE, DELAY, DUPLICATE
	}
}
