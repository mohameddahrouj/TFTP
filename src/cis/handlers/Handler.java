package cis.handlers;

import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Base class of the WriteHandler and ReadHandler
 * @author Mohamed Dahrouj, Ali Farah, Lava Tahir, Tosin Oni, Vanja Veselinovic
 *
 */
public abstract class Handler {

    protected DatagramSocket sendAndReceiveSocket;

    protected byte prefix;
    protected InetAddress address;
    protected int port;
    protected String file;


    public Handler(DatagramSocket sendAndReceiveSocket, byte prefix, InetAddress address, int port, String file)
    {
        //Initialize variables
        this.sendAndReceiveSocket = sendAndReceiveSocket;
        this.prefix = prefix;
        this.address = address;
        this.port = port;
        this.file = file;
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

    public abstract void process();
}
