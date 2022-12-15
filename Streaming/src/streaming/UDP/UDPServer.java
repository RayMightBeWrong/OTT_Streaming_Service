package streaming.UDP;

import java.util.Timer;

import overlay.state.NodeState;

public class UDPServer extends Thread{
    public static final int PORT = 25000;
    private Timer timer;
    private boolean running;
    private VideoSender sender;

    public UDPServer(NodeState state){
        this.running = true;
        this.sender = new VideoSender("movie.Mjpeg", state);
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
