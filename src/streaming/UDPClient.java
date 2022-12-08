package streaming;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPClient extends Thread{
    public void run(){
        try {
            DatagramSocket socket = new DatagramSocket(UDPServer.PORT);
            DatagramSocket sender = new DatagramSocket();
            InetAddress ip = InetAddress.getLocalHost();
            System.out.println("opened UDP client");
            
            while(true){
                byte[] buf = new byte[VideoSender.bufLength];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);

                socket.receive(packet);
                RTPPacket rtp_packet = new RTPPacket(packet.getData(), packet.getLength());
                System.out.println("Got RTP packet with SeqNum # "+rtp_packet.getsequencenumber()+" TimeStamp "+rtp_packet.gettimestamp()+" ms, of type "+rtp_packet.getpayloadtype());
            
                DatagramPacket senddp = new DatagramPacket(buf, buf.length, ip, Cliente.RTP_RCV_PORT);
                sender.send(senddp);
            }

            //socket.close();
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
