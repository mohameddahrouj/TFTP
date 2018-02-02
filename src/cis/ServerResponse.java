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
    private DatagramSocket sendingSocket;
    private Request request;
    private Handler handler;


    public ServerResponse(DatagramPacket receivedPacket, Request request){
        try {
            this.receivedPacket = receivedPacket;
            this.request = request;
            this.sendingSocket = new DatagramSocket();
            if(request == Request.READ)
                this.handler = new WriteHandler(sendingSocket,receivedPacket.getAddress(),receivedPacket.getPort());
            else
                this.handler = new ReadHandler(sendingSocket,receivedPacket.getAddress(),receivedPacket.getPort());

        }
        catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void run(){
        try {

            if(this.request == Request.WRITE)
            {
                ReadHandler readHandler = (ReadHandler)handler;
                readHandler.sendAck(0);
            }
            handler.process();
            this.sendingSocket.close();
        }
        catch(Exception e){
            System.out.println("Packet is invalid!");
            System.exit(1);
        }
    }
}
