package cis;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class ReadHandler extends Handler {

    private static final byte prefixNumber = 4;
    private List<Byte> buffer;
    private final static int maxBlockLength = 516;

    public ReadHandler(DatagramSocket socket,InetAddress address, int port, String file)
    {
        super(socket,prefixNumber, address, port, file);
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

            isFinalPacket = isFinalPacket(receivedPacket);
        }

        writeToFile();
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

    private int getBlockNumber(byte[] data)
    {
       return ((data[2] << 8) + data[3]);
    }

    private void bufferData(DatagramPacket packet)
    {
        byte[] data = packet.getData();
        for (int i = 4; i< data.length; i++) {
            this.buffer.add(data[i]);
        }
    }

    private void writeToFile()
    {
        try {
            FileOutputStream stream = new FileOutputStream(this.file); // hardcoded for now
            stream.write(getData());
            stream.close();
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

    private boolean isFinalPacket(DatagramPacket receivedPacket)
    {
        return Resources.truncateData(receivedPacket.getData()).length < maxBlockLength;
    }

}
