package streaming.UDP;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

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
    Map<Integer, RTPPacket> videoPackets;

    byte[] buf;
    int bufLength = 15000;
  
    public UDPMiddleMan(NodeState state) {
        this.state = state;

        buf = new byte[bufLength];
        try{
            sender = new DatagramSocket();
            receiver = new DatagramSocket(UDPServer.PORT);
            rcvdp = new DatagramPacket(buf, buf.length);
            videoPackets = new HashMap<>();
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
                videoPackets.put(rtp_packet.getsequencenumber() - 1, rtp_packet);
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

    public void sendTo(StreamLink stream){
        Timer timer = new Timer();
        timer.schedule(new TemporarySender(state, videoPackets, stream), 0, VideoSender.FRAME_PERIOD);
    }

    public void reset(){
        this.videoPackets = new HashMap<>();
    }

    class TemporarySender extends TimerTask{
        private int frame_nr;
        private Map<Integer, RTPPacket> videoPackets;
        private NodeState state;
        private StreamLink stream;

        TemporarySender(NodeState state, Map<Integer, RTPPacket> videoPackets, StreamLink stream){
            this.frame_nr = 0;
            this.videoPackets = videoPackets;
            this.state = state;
            this.stream = stream;
        }

        public void run(){
            try{
                RTPPacket rtp_packet = this.videoPackets.get(frame_nr);
                rtp_packet.changeStreamID(this.stream.getStreamID());
                rtp_packet.printheader();
    
                int streamID = rtp_packet.getStreamID();
                StreamLink stream = this.state.getStreamFromID(streamID);
                String nextNode = stream.findNextNode(this.state.getSelf(), false);
    
                NodeLink link = this.state.getLinkTo(nextNode);
                byte[] buffer = rtp_packet.getContent();
                DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length, link.getViaInterface(), UDPServer.PORT);
                sender.send(sendPacket);
                frame_nr ++;
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
