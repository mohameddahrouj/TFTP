package cis.handlers;

import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

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

    public Handler(DatagramSocket sendAndReceiveSocket, byte prefix, InetAddress address, int port, String filePath)
    {
        //Initialize variables
        this.sendAndReceiveSocket = sendAndReceiveSocket;
        this.prefix = prefix;
        this.address = address;
        this.port = port;
        this.filePath = filePath;
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
    
    protected boolean doesFileExist()
    {
    	File file = new File(this.filePath);
    	
    	return file.exists() && !file.isDirectory();
    }
    
    protected void sendErrorPacket(IOErrorType errorType)
    {
    	byte[] data = errorType.createErrorPacketData();
    	DatagramPacket packet = new DatagramPacket(data, data.length,address,port);
    	Resources.sendPacket(packet, sendAndReceiveSocket);
    }

    public abstract void process();
}
