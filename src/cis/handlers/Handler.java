package cis.handlers;

import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import cis.utils.IOErrorType;
import cis.utils.Request;
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
        ack[2] = (byte) ( (blockNumber >> 8) & 0xFF);
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
    
    protected void processPacket(DatagramPacket receivedPacket)
    {	
		Request type = Resources.packetRequestType(receivedPacket);	
		if(type == Request.INVALID)
		{
			System.out.println("Recieved a packet with an incorrect opcode. Sending error packet then exiting");
			this.sendErrorPacket(IOErrorType.IllegalOperation);
			System.exit(1);
		}
    }
    
    protected IOErrorType getErrorType(byte[] data)
    {
    	int errorCode = data[3];
    	if(IOErrorType.FileNotFound.getErrorCode() == errorCode)
    	{
    		return IOErrorType.FileNotFound;
    	}
    	else if(IOErrorType.AccessViolation.getErrorCode() == errorCode)
    	{
    		return IOErrorType.AccessViolation;
    	}
    	else if(IOErrorType.DiskFull.getErrorCode() == errorCode)
    	{
    		return IOErrorType.DiskFull;
    	}
    	else if(IOErrorType.IllegalOperation.getErrorCode() == errorCode)
    	{
    		return IOErrorType.IllegalOperation;
    	}
    	else if(IOErrorType.UnkownTransferID.getErrorCode() == errorCode)
    	{
    		return IOErrorType.UnkownTransferID;
    	}
    	else if(IOErrorType.FileExists.getErrorCode() == errorCode)
    	{
    		return IOErrorType.FileExists;
    	}
    	
    	return null;
    }

    public abstract void process();
    
}
