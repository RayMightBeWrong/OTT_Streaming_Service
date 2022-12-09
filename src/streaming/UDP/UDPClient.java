package streaming.UDP;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import streaming.OTTStreaming;

public class UDPClient extends Thread{
    private InetAddress ip;

    public UDPClient(InetAddress ip){
        this.ip = ip;
    }

    public void run(){
        try {
            DatagramSocket socket = new DatagramSocket(UDPServer.PORT);
            DatagramSocket sender = new DatagramSocket();
            
            while(true){
                byte[] buf = new byte[VideoSender.bufLength];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);

                socket.receive(packet);
                RTPPacket rtp_packet = new RTPPacket(packet.getData(), packet.getLength());
                System.out.println("Got RTP packet with SeqNum # "+rtp_packet.getsequencenumber()+" TimeStamp "+rtp_packet.gettimestamp()+" ms, of type "+rtp_packet.getpayloadtype());
            
                DatagramPacket senddp = new DatagramPacket(buf, buf.length, ip, OTTStreaming.RTP_PORT);
                sender.send(senddp);
            }

            //socket.close();
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
