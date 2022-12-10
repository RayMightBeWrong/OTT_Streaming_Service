package streaming.UDP;

import java.net.InetAddress;
import java.util.Timer;

public class UDPServer extends Thread{
    public static final int PORT = 25000;
    private InetAddress ip;
    private Timer timer;
    private boolean running;
    private VideoSender sender;

    public UDPServer(InetAddress ip){
        this.ip = ip;
        this.running = true;
        this.sender = new VideoSender(ip, "movie.Mjpeg");
    }
    
    public void run(){
        timer = new Timer();
        timer.schedule(this.sender, 0, VideoSender.FRAME_PERIOD);
    }

    public void pauseSender() throws Exception{
        System.out.println("pause");
        if(running){
            sender.pause();
            this.running = false;
        }
        else{
            sender.resume();
            this.running = true;
        }
    }

    public void resumeSender(){
        this.running = true;
    }
}
