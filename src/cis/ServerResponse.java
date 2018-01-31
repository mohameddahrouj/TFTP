package cis;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/*
*  ServerResponse is the class the server uses to send a response. It is a thread, so the
*  server will create multiple server responses to handle the incoming requests.
*  Last edited January 30th, 2018
*  @author Mohamed Dahrouj, Lava Tahir
* **/

public class ServerResponse  implements Runnable{
    private DatagramPacket receivedPacket;
    private DatagramSocket sendingSocket, serverSocket;
    private byte[] readRequest, writeRequest, readResponse, writeResponse;
    private InetAddress address;
    ByteArrayOutputStream byteArrayOutputStream;


    public ServerResponse(DatagramPacket receivedPacket, byte[] readRequest, byte[] writeRequest,
                          InetAddress address, DatagramSocket sendingSocket, DatagramSocket serverSocket){
        this.receivedPacket = receivedPacket;
        this.readRequest = readRequest;
        this.writeRequest = writeRequest;
        this.address = address;
        this.sendingSocket = sendingSocket;
        this.serverSocket = serverSocket;
        //Initialize single byte array output stream
        //Format Read and Write Requests
        //Reset once done
        byteArrayOutputStream = new ByteArrayOutputStream();
    }
    /**
     * Determine if packet is valid
     * @param packet Packet to check
     * @return True if packet is valid
     */
    private boolean isPacketValid(DatagramPacket packet) {

        if(packetRequestType(packet)==Request.INVALID) {
            return false;
        }
        return true;
    }
    /**
     * Determine if packet is a read or write request
	 * @param packet Packet
	 * @return Request type of packet
	 */
    private Request packetRequestType(DatagramPacket packet) {
        //The second element of a read request should be 1
        //Whereas the second element of a write request should be 2
        if(packet.getData()[1] == (byte) 0x01) {
            return Request.READ;
        }
        else if(packet.getData()[1] == (byte) 0x02) {
            return Request.WRITE;
        }
        return Request.INVALID;
    }

    /**
     * Method to send packet to the error simulator to be forwarded to client.
     * @throws Exception
     */
    private void send() throws Exception{
        if(isPacketValid(receivedPacket)) {
            if(packetRequestType(receivedPacket)==Request.READ) {
                byte[] readResponse = formatReadResponse();
                System.out.println("\nServer: Forming new read packet");
                DatagramPacket newPacket = new DatagramPacket(readResponse, readResponse.length, address,  receivedPacket.getPort());
                Resources.printPacketInformation(newPacket);
                Resources.sendPacket(newPacket, sendingSocket);
            }
            else if(packetRequestType(receivedPacket)==Request.WRITE) {
                System.out.println("\nServer: Forming new write packet");
                byte[] writeResponse = formatWriteResponse();
                DatagramPacket newPacket = new DatagramPacket(writeResponse, writeResponse.length, address,  receivedPacket.getPort());
                Resources.printPacketInformation(newPacket);

                System.out.println("\nServer: Sending packet to intermediate host");
                Resources.printPacketInformation(newPacket);
                Resources.sendPacket(newPacket, sendingSocket);
                System.out.println("Server: Packet sent!\n");
            }

        }
        else {
            serverSocket.close();
            sendingSocket.close();
            throw new Exception("Invalid packet detected.");

        }
    }
    /**
     * Read response format as per TFTP Specification. Responds with Data block block number 1 and no bytes of data.
     * @throws IOException
     */
    private byte[] formatReadResponse() throws IOException{
        byteArrayOutputStream.reset();
        //opcode is 2 bytes 03
        byteArrayOutputStream.write(0);
        byteArrayOutputStream.write(3);
        //block number 1
        byteArrayOutputStream.write(0);
        byteArrayOutputStream.write(1);
        //0 bytes of data. so no more writes.

        return byteArrayOutputStream.toByteArray();
    }
    /**
     * Write response format as per TFTP Specification. Response with ACK block.
     * @throws IOException
     */
    private byte[] formatWriteResponse() throws IOException{
        byteArrayOutputStream.reset();
        //opcode is 2 bytes 03
        byteArrayOutputStream.write(0);
        byteArrayOutputStream.write(4);
        //block number 0
        byteArrayOutputStream.write(0);
        byteArrayOutputStream.write(0);

        return byteArrayOutputStream.toByteArray();
    }
    @Override
    public void run(){
        try {
            this.send();
        }
        catch(Exception e){
            System.out.println("Packet is invalid!");
            System.exit(1);
        }
    }
}
