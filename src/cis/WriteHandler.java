package cis;

import java.io.*;
import java.net.DatagramPacket;
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
        byte[] fileToCopy = readFileAndConvertToByteArray("client.txt"); //hardcoded for now
        int numBytesCopied = 0;
        //wait for ack to start the writing process
        DatagramPacket receivedPacket = Resources.receivePacket(this.sendAndReceiveSocket);
        System.out.println("Client: Packet received:");
        Resources.printPacketInformation(receivedPacket);

        return false;
    }

    private byte[] readFileAndConvertToByteArray(String filename){
        File file = new File(filename);
        byte[] fileBytes = new byte[(int) file.length()];

        try {
            FileInputStream fis = new FileInputStream(file);
            fis.read(fileBytes); //read file into bytes[]
            fis.close();
        }
        catch(FileNotFoundException ex) {
            System.out.println(
                    "No name found for '" + filename + "'");
        }
        catch(IOException ex) {
            System.out.println(
                    "IO Exception when reading '" + filename + "'");
        }
        return fileBytes;
    }

    private void writeDataFromFileBytes(int blockNumber, byte[] fileBytes){

    }
    {

    }
}
