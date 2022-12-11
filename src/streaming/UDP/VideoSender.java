package streaming.UDP;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.TimerTask;

import overlay.TCP.TCPCommunicator;
import overlay.state.StreamLink;

public class VideoSender extends TimerTask{
    private DatagramPacket senddp;
    private DatagramSocket RTPsocket;
    private int RTP_PORT = 25000;
    private InetAddress ownIP;
    private InetAddress clientIP;
    private byte[] buf;
    public static int bufLength = 15000;

    public static int FRAME_PERIOD = 42;
    private int imagenb = 0;
    private VideoStream video;
    private int VIDEO_LENGTH = 500;
    private boolean running;
    private StreamLink stream;
    

    public VideoSender(InetAddress ownIP, InetAddress clientIP, String videoFileName, StreamLink stream){
        this.buf = new byte[bufLength];
        this.running = true;
        
        try {
            this.RTPsocket = new DatagramSocket();
            this.ownIP = ownIP;
            this.clientIP = clientIP;
            this.stream = stream;
            this.video = new VideoStream(videoFileName);
        } catch (SocketException e) {
            System.out.println("Servidor: erro no socket: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Servidor: erro no video: " + e.getMessage());
        }
    }

    public void pause(){
        this.running = false;
    }

    public void resume(){
        this.running = true;
    }

    public void run(){
        if(running){
            if (imagenb < VIDEO_LENGTH){
                imagenb++;

                try {
                    int imageLength = video.getNextFrame(buf);
                    RTPPacket RTPPacket = new RTPPacket(this.stream.getStreamID(), imagenb, imagenb * FRAME_PERIOD, buf, imageLength);
                    int packetLength = RTPPacket.getlength();
  
                    byte[] packet_bits = new byte[packetLength];
                    RTPPacket.getpacket(packet_bits);
  
                    senddp = new DatagramPacket(packet_bits, packetLength, clientIP, RTP_PORT);
                    RTPsocket.send(senddp);
  
                    RTPPacket.printheader();
                }
                catch(Exception ex){
                    System.out.println("Exception caught: "+ex);
                    System.exit(0);
                }
            }
            else{
                this.running = false;
                TCPCommunicator client;
                client = new TCPCommunicator(null, this.ownIP, TCPCommunicator.END_STREAM_CLIENT, this.stream.convertLinkToArgs());
                client.run();
            }
        }
    }
}
