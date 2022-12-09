package streaming.UDP;

import java.net.InetAddress;
import java.util.Timer;

public class UDPServer extends Thread{
    public static final int PORT = 25000;
    private InetAddress ip;

    public UDPServer(InetAddress ip){
        this.ip = ip;
    }
    
    public void run(){
        Timer sender = new Timer();
        sender.schedule(new VideoSender(ip, "movie.Mjpeg"), 0, VideoSender.FRAME_PERIOD);
    }
}
