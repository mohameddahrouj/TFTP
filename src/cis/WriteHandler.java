package cis;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class WriteHandler extends Handler {

    private static final byte prefixNumber = 3;


    public WriteHandler(DatagramSocket socket,InetAddress address, int port)
    {
        super(socket,prefixNumber, address, port);
    }

    @Override
    public boolean process() {

        return false;
    }
}
