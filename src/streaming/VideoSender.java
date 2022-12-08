package streaming;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.TimerTask;

public class VideoSender extends TimerTask{
    private DatagramPacket senddp;
    private DatagramSocket RTPsocket;
    private int RTP_PORT = 25000;
    private InetAddress clientIP;
    private byte[] buf;
    public static int bufLength = 15000;

    public static int FRAME_PERIOD = 42;
    private int imagenb = 0;
    private VideoStream video;
    private int MJPEG_TYPE = 26;
    private int VIDEO_LENGTH = 500;
    

    public VideoSender(InetAddress clientIP, String videoFileName){
        this.buf = new byte[this.bufLength];
        
        try {
            this.RTPsocket = new DatagramSocket();
            this.clientIP = clientIP;
            this.video = new VideoStream(videoFileName);
        } catch (SocketException e) {
            System.out.println("Servidor: erro no socket: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Servidor: erro no video: " + e.getMessage());
        }
    }

    public void run(){
        if (imagenb < VIDEO_LENGTH){
            imagenb++;

            try {
                int imageLength = video.getNextFrame(buf);
                RTPPacket RTPPacket = new RTPPacket(MJPEG_TYPE, imagenb, imagenb * FRAME_PERIOD, buf, imageLength);
  
                int packetLength = RTPPacket.getlength();
  
                byte[] packet_bits = new byte[packetLength];
                RTPPacket.getpacket(packet_bits);
  
                senddp = new DatagramPacket(packet_bits, packetLength, clientIP, RTP_PORT);
                RTPsocket.send(senddp);
  
                System.out.println("Send frame #"+imagenb);
                RTPPacket.printheader();
            }
            catch(Exception ex){
                System.out.println("Exception caught: "+ex);
                System.exit(0);
            }
        }
        else{
            // TODO - começar de novo
        }
    }
}
