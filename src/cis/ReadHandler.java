package cis;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class ReadHandler extends Handler {

    private static final byte prefixNumber = 4;
    private final static int maxBlockLength = 516;

    public ReadHandler(DatagramSocket socket,InetAddress address, int port)
    {
        super(socket,prefixNumber, address, port);
    }

    @Override
    public boolean process() {

        //Wait to get data
        DatagramPacket receivedPacket = Resources.receivePacket(this.sendAndReceiveSocket);
        System.out.println("Client: Packet received:");
        Resources.printPacketInformation(receivedPacket);

        writeToFile(receivedPacket);

        //send Ack for data received
        sendAck(getBlockNumber(receivedPacket.getData()));

        return isFinalPacket(receivedPacket);
    }

    private void sendAck(int blockNumber)
    {
        byte[] ack = super.getPrefix(blockNumber);
        DatagramPacket packet = new DatagramPacket(ack, ack.length,address,port);
        System.out.println("Sending Ack:");
        Resources.printPacketInformation(packet);
        Resources.sendPacket(packet, this.sendAndReceiveSocket);
        System.out.println("Ack sent\n");
    }

    private int getBlockNumber(byte[] data)
    {
       return ByteBuffer.wrap(new byte[] {data[2], data[3]} ).getInt();
    }

    private void writeToFile(DatagramPacket receivedPacket)
    {
        try {
            FileOutputStream stream = new FileOutputStream("client.txt"); // hardcoded for now
            stream.write(receivedPacket.getData());
            stream.close();
        }
        catch (IOException exception)
        {
            System.out.println("Error copying data to file");
            exception.printStackTrace();
            System.exit(1);
        }
    }

    private boolean isFinalPacket(DatagramPacket receivedPacket)
    {
        return receivedPacket.getData().length < maxBlockLength;
    }

}
