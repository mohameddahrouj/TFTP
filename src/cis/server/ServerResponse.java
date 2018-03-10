package cis.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import cis.handlers.Handler;
import cis.handlers.ReceiverHandler;
import cis.handlers.SenderHandler;
import cis.utils.Request;
import cis.utils.Resources;

/**
 * ServerResponse is the class the server uses to send a response. It is a thread, so the
 * server will create multiple server responses to handle the incoming requests.
 * Last edited January 30th, 2018
 * @author Mohamed Dahrouj, Ali Farah, Lava Tahir, Tosin Oni, Vanja Veselinovic
 *
 */
public class ServerResponse  implements Runnable{
    private DatagramSocket sendingSocket;
    private Handler handler;

    public ServerResponse(DatagramPacket receivedPacket, Request request, String filePath){
        try {
            this.sendingSocket = new DatagramSocket();
            if(request == Request.READ)
            {
                // if the request is a read then you will be sending data from a file to the client.
                this.handler = new SenderHandler(
                        sendingSocket,
                        receivedPacket.getAddress(),
                        receivedPacket.getPort(),
                        filePath,
                        Resources.SERVER);
            }
            else
            {
                // if the request is a write then you will be receiving data from a file.
                this.handler = new ReceiverHandler(
                        sendingSocket,
                        receivedPacket.getAddress(),
                        receivedPacket.getPort(),
                        filePath,
                        "Server",
                        Resources.SERVER);
                //Send the ack initially
                //((ReceiverHandler)handler).sendAck(0);
            }

        }
        catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * If it is read request then send the contents of a file to the client.
     * If it is write request then receive contents of file from the client and write it to a file.
     */
    @Override
    public void run(){
        try {
        	
            handler.process();
            this.sendingSocket.close();

        }
        catch(Exception e){
            System.out.println("Packet is invalid!");
            System.exit(1);
        }
    }
}
