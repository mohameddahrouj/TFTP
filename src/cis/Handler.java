package cis;

import java.net.DatagramSocket;
import java.net.InetAddress;

public abstract class Handler {

    protected DatagramSocket sendAndReceiveSocket;

    protected byte prefix;
    protected InetAddress address;
    protected int port;


    public Handler(DatagramSocket sendAndReceiveSocket, byte prefix, InetAddress address, int port)
    {
        this.sendAndReceiveSocket = sendAndReceiveSocket;
        this.prefix = prefix;
        this.address = address;
        this.port = port;
    }

    protected byte[] getPrefix(int blockNumber) {

        byte[] ack = new byte[4];
        ack[0] = 0;
        ack[1] = prefix;
        ack[2] = (byte) ( (blockNumber >> 16) & 0xFF);
        ack[3] = (byte) ( (blockNumber >> 24) & 0xFF);
        return ack;
    }

    public abstract void process();
}
