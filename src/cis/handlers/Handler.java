package cis.handlers;

import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import cis.utils.IOErrorType;
import cis.utils.Resources;

/**
 * Base class of the SenderHandler and ReceiverHandler
 * @author Mohamed Dahrouj, Ali Farah, Lava Tahir, Tosin Oni, Vanja Veselinovic
 *
 */
public abstract class Handler {

    protected DatagramSocket sendAndReceiveSocket;

    protected byte prefix;
    protected InetAddress address;
    protected int port;
    protected String filePath;
    protected int requester;

    public Handler(DatagramSocket sendAndReceiveSocket, byte prefix, InetAddress address, int port, String filePath, int requester)
    {
        //Initialize variables
        this.sendAndReceiveSocket = sendAndReceiveSocket;
        try {
			this.sendAndReceiveSocket.setSoTimeout(1000);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        this.prefix = prefix;
        this.address = address;
        this.port = port;
        this.filePath = filePath;
        this.requester = requester;
    }

    /**
     * Creates the beginning of the ACK and Data packet
     * @param blockNumber
     * @return ack in byte array form
     */
    protected byte[] getPrefix(int blockNumber) {

        byte[] ack = new byte[4];
        ack[0] = 0;
        ack[1] = prefix;
        ack[2] = (byte) ( (blockNumber >> 7) & 0xFF);
        ack[3] = (byte) ( (blockNumber) & 0xFF);
        return ack;
    }
     
    /**
     * sends the error packet associated with the error
     * @param errorType
     */
    protected void sendErrorPacket(IOErrorType errorType)
    {
    	byte[] data = errorType.createErrorPacketData();
    	DatagramPacket packet = new DatagramPacket(data, data.length,address,port);
    	Resources.sendPacket(packet, sendAndReceiveSocket);
    }

    public abstract void process();
}
