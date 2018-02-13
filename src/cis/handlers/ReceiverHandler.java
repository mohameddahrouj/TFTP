package cis.handlers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import cis.utils.IOErrorType;
import cis.utils.Resources;

/**
 * This class will receive data from a file and write it to a local file.
 * @author Mohamed Dahrouj, Ali Farah, Lava Tahir, Tosin Oni, Vanja Veselinovic
 */
public class ReceiverHandler extends Handler {

    private static final byte prefixNumber = 4;
    private List<Byte> buffer;
    private final static int maxBlockLength = 516;
    private FileOutputStream fileStream;

    public ReceiverHandler(DatagramSocket socket,InetAddress address, int port, String fileName, String directory)
    {
        super(socket,prefixNumber, address, port, fileName);
        this.fileStream = this.createFile(directory);
        this.buffer = new ArrayList<>();
    }

    @Override
    public void process() {

        boolean isFinalPacket = false;
        while(!isFinalPacket) {
            //Wait to get data
            System.out.println("Waiting to get Data");
            DatagramPacket receivedPacket = Resources.receivePacket(this.sendAndReceiveSocket);
            System.out.println("Data Received: ");
            Resources.printPacketInformation(receivedPacket);

            //send Ack for data received
            sendAck(getBlockNumber(receivedPacket.getData()));

            bufferData(receivedPacket);
            writeToFile();
            isFinalPacket = isFinalPacket(receivedPacket);
        }

        try {
			this.fileStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    public void sendAck(int blockNumber)
    {
        byte[] ack = super.getPrefix(blockNumber);
        DatagramPacket packet = new DatagramPacket(ack, ack.length,address,port);
        System.out.println("Sending Ack for block: " + blockNumber);
        Resources.printPacketInformation(packet);
        Resources.sendPacket(packet, this.sendAndReceiveSocket);
        System.out.println("Ack sent\n");
    }
    
    private FileOutputStream createFile(String directory)
    {
    	try
    	{
    		Path path = Paths.get(this.filePath);
    		Path filePath = Files.createFile(Paths.get("./" + directory +"/" +path.getFileName()));
	    	File file = new File(filePath.toString());
    		return new FileOutputStream(file);
    	}
    	catch(FileAlreadyExistsException  e)
    	{
    		super.sendErrorPacket(IOErrorType.FileExists);
    		//e.printStackTrace();
    		System.exit(1);
    	}
    	catch(IOException e)
    	{
    		super.sendErrorPacket(IOErrorType.FileExists);
    		e.printStackTrace();
    		System.exit(1);
    	}
    	return null;
    }

    private int getBlockNumber(byte[] data)
    {
       return ((data[2] << 8) + data[3]);
    }

    /**
     * Save the data received from the sender.
     * @param packet The packet received from the sender.
     */
    private void bufferData(DatagramPacket packet)
    {
        byte[] data = packet.getData();
        for (int i = 4; i< data.length; i++) {
            this.buffer.add(data[i]);
        }
    }

    /**
     * Write the data received to a local file.
     */
    private void writeToFile()
    {
        try {
            fileStream.write(getData());
            this.buffer.clear();
        }
        catch (IOException exception)
        {
            System.out.println("Error copying data to file");
            exception.printStackTrace();
            System.exit(1);
        }
    }

    private byte[] getData()
    {
        byte[] data = new byte[this.buffer.size()];

        for (int i =0; i< this.buffer.size(); i++)
        {
            data[i] = this.buffer.get(i);
        }
        return data;
    }

    /**
     * Checks if the packet is the if final packet
     * @param receivedPacket The packet received from the sender.
     * @return
     */
    private boolean isFinalPacket(DatagramPacket receivedPacket)
    {
        return Resources.truncateData(receivedPacket.getData()).length < maxBlockLength;
    }

}
