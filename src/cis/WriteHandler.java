package cis;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

public class WriteHandler extends Handler {

    private static final byte prefixNumber = 3;
    private static final int maxBlockSize = 512;

    private int blockNumber;
    private byte[] fileData;

    public WriteHandler(DatagramSocket socket,InetAddress address, int port, String file)
    {
        super(socket,prefixNumber, address, port,file);
        this.blockNumber = 0;
        this.fileData = readFileAndConvertToByteArray(this.file); //hardcoded for now
    }

    private boolean isFinalPacket() {
        return this.blockNumber * maxBlockSize > this.fileData.length;
    }

    @Override
    public void process() {
        boolean isFinalPacket = false;

        while(!isFinalPacket) {
            this.sendData();
            this.blockNumber++; // increase the blockNumber after each write
            this.waitForACK();
            isFinalPacket =  isFinalPacket();
        }
    }

    private void sendData()
    {
        System.out.println("Sending Block: " + this.blockNumber);
        DatagramPacket packet = createWritePacket();
        Resources.printPacketInformation(packet);
        Resources.sendPacket(packet, this.sendAndReceiveSocket);
        System.out.println("Data sent\n");
    }

    public void waitForACK()
    {
        System.out.println("Wait to Receive ACK");
        //wait for ack to start the writing process
        DatagramPacket receivedPacket = Resources.receivePacket(this.sendAndReceiveSocket);
        System.out.println("ACK Received:");
        Resources.printPacketInformation(receivedPacket);
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
            System.exit(1);

        }
        catch(IOException ex) {
            System.out.println(
                    "IO Exception when reading '" + filename + "'");
            System.exit(1);

        }
        return fileBytes;
    }

    private DatagramPacket createWritePacket()
    {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(super.getPrefix(blockNumber));
            outputStream.write(getFileData());
            byte[] data = outputStream.toByteArray();
            DatagramPacket packet = new DatagramPacket(data, data.length,address,port);
            return packet;
        }
        catch(IOException ex) {
            System.out.println(
                    "IO Exception when reading creating write packet");
            System.exit(1);
        }

        return null;
    }

    private byte[] getFileData()
    {
        int start = this.blockNumber * maxBlockSize;
        int end = start + maxBlockSize;

        if(end > this.fileData.length)
        {
            end = this.fileData.length;
        }

        return Arrays.copyOfRange(this.fileData,start,end);
    }

}
