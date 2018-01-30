package cis;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ServerResponse  implements Runnable{
    private DatagramPacket receivedPacket;
    private DatagramSocket sendingSocket, serverSocket;
    private byte[] readRequest, writeRequest;
    private InetAddress address;


    public ServerResponse(DatagramPacket receivedPacket, byte[] readRequest, byte[] writeRequest,
                          InetAddress address, DatagramSocket sendingSocket, DatagramSocket serverSocket){
        this.receivedPacket = receivedPacket;
        this.readRequest = readRequest;
        this.writeRequest = writeRequest;
        this.address = address;
        this.sendingSocket = sendingSocket;
        this.serverSocket = serverSocket;
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
    private void send() throws Exception{
        if(isPacketValid(receivedPacket)) {
            if(packetRequestType(receivedPacket)==Request.READ) {
                System.out.println("\nServer: Forming new read packet");
                DatagramPacket newPacket = new DatagramPacket(readRequest, readRequest.length, address,  receivedPacket.getPort());
                Resources.printPacketInformation(newPacket);
                Resources.sendPacket(newPacket, sendingSocket);
            }
            else if(packetRequestType(receivedPacket)==Request.WRITE) {
                System.out.println("\nServer: Forming new write packet");
                DatagramPacket newPacket = new DatagramPacket(writeRequest, writeRequest.length, address,  receivedPacket.getPort());
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
