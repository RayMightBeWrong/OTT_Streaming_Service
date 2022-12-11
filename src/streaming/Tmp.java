package streaming;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import overlay.TCP.TCPCommunicator;
import streaming.UDP.RTPPacket;

public class Tmp {
    public static void main(String[] args){
        try{
            InetAddress ip = InetAddress.getByName(args[0]);
            TCPCommunicator client;
            client = new TCPCommunicator(null, ip, TCPCommunicator.OPEN_STREAM_CLIENT);
            client.run();

            tmpMethod();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void tmpMethod() throws Exception{
        byte[] cBuf = new byte[15000];

        DatagramSocket RTPsocket = new DatagramSocket(OTTStreaming.RTP_PORT);
        RTPsocket.setSoTimeout(5000);

        while(true){
            DatagramPacket rcvdp = new DatagramPacket(cBuf, 15000);

            try{
                RTPsocket.receive(rcvdp);

                RTPPacket rtp_packet = new RTPPacket(rcvdp.getData(), rcvdp.getLength());
                rtp_packet.printheader();
            }
            catch(InterruptedIOException iioe){
                System.out.println("Nothing to read");
            }
            catch(IOException ioe){
                System.out.println("Exception caught: " + ioe);
            }
        }
    }
}
