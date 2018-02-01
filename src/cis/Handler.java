package cis;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public abstract class Handler {

    protected DatagramSocket sendAndReceiveSocket;

    protected byte prefix;
    protected InetAddress address;
    protected int port;

    private final static int maxBlockLength = 516;

    public Handler(DatagramSocket sendAndReceiveSocket, byte prefix, InetAddress address, int port)
    {
        this.sendAndReceiveSocket = sendAndReceiveSocket;
        this.prefix = prefix;
        this.address = address;
        this.port = port;
    }

    protected byte[] getPrefix(int blockNumber) {

        byte[] ack = new byte[4];
        byte[] blockByte = ByteBuffer.allocate(2).putInt(blockNumber).array();
        ack[0] = 0;
        ack[1] = prefix;
        ack[2] = blockByte[1];
        ack[3] = blockByte[0];
        return ack;
    }

    public abstract boolean process();

    protected boolean isFinalPacket(DatagramPacket receivedPacket)
    {
        return receivedPacket.getData().length < maxBlockLength;
    }

}
