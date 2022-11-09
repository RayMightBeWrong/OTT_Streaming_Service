package overlay;

import java.net.InetAddress;
import java.net.ServerSocket;

public class NeighborController extends Thread{
    private InetAddress neighborIP;

    public NeighborController(InetAddress neighborIP){
        this.neighborIP = neighborIP;
    }

    public void run(){
        try{
            ServerSocket server = new ServerSocket();
        }
        catch(Exception e){
            
        }
    }
}
