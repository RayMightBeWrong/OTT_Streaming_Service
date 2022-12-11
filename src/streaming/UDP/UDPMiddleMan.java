package streaming.UDP;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;

import overlay.state.NodeLink;
import overlay.state.NodeState;
import overlay.state.StreamLink;
import streaming.OTTStreaming;

public class UDPMiddleMan extends Thread{
    NodeState state;
    DatagramPacket senddp;
    DatagramPacket rcvdp;
    DatagramSocket receiver;
    DatagramSocket sender;

    byte[] buf;
    int bufLength = 15000;
  
    public UDPMiddleMan(NodeState state) {
        this.state = state;

        buf = new byte[bufLength];
        try{
            sender = new DatagramSocket();
            receiver = new DatagramSocket(UDPServer.PORT);

            rcvdp = new DatagramPacket(buf, buf.length);
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

                RTPPacket rtp_packet = new RTPPacket(rcvdp.getData(), rcvdp.getLength());
                rtp_packet.printheader();

                int streamID = rtp_packet.getStreamID();
                StreamLink stream = this.state.getStreamFromID(streamID);
                String nextNode = stream.findNextNode(this.state.getSelf(), false);

                if (nextNode.equals(this.state.getSelf())){
                    List<InetAddress> ips = this.state.getSelfIPs();
                    senddp = new DatagramPacket(buf, buf.length, ips.get(0), OTTStreaming.RTP_PORT);
                    sender.send(senddp);
                }
                else{
                    NodeLink link = this.state.getLinkTo(nextNode);
                    senddp = new DatagramPacket(buf, buf.length, link.getViaInterface(), UDPServer.PORT);
                    sender.send(senddp);
                }                
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
