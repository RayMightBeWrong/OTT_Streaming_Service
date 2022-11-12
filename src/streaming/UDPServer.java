package streaming;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UDPServer extends Thread{
    
    public void run(){
        try {
            DatagramSocket socket = new DatagramSocket(4445);
            
            while(true){
                byte[] buf = new byte[256];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                try {
                    socket.receive(packet);
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            socket.close();
        } 
        catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
