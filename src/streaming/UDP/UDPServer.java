package streaming.UDP;

import java.net.InetAddress;
import java.util.Timer;

public class UDPServer extends Thread{
    public static final int PORT = 25000;
    private Timer timer;
    private boolean running;
    private VideoSender sender;

    public UDPServer(InetAddress ownIP, InetAddress clientIP){
        this.running = true;
        this.sender = new VideoSender(ownIP, clientIP, "movie.Mjpeg");
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

    public void cancelSender(){
        timer.cancel();
    }
}
