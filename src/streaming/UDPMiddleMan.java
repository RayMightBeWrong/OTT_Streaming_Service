package streaming;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UDPMiddleMan extends Thread{
    DatagramPacket senddp;
    DatagramPacket rcvdp;
    DatagramSocket receiver;
    DatagramSocket sender;
    InetAddress ip;

    byte[] buf;
    int bufLength = 15000;
  
    public UDPMiddleMan(InetAddress ip) {
        buf = new byte[bufLength];
        try{
            sender = new DatagramSocket();
            receiver = new DatagramSocket(UDPServer.PORT);
            rcvdp = new DatagramPacket(buf, buf.length);
            this.ip = ip;
        }
        catch(SocketException e){
            System.out.println("Servidor: erro no socket: " + e.getMessage());
        }
        catch (Exception e){
            System.out.println("Servidor: erro no video: " + e.getMessage());
        }
    }

    public void run(){
        try{
            while(true){
                receiver.receive(rcvdp);
                //RTPPacket rtp_packet = new RTPPacket(rcvdp.getData(), rcvdp.getLength());
                //int payload_length = rtp_packet.getpayload_length();
                //byte[] payload = new byte[payload_length];
                //rtp_packet.getpayload(payload);

                senddp = new DatagramPacket(buf, buf.length, ip, UDPServer.PORT);
                sender.send(senddp);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
