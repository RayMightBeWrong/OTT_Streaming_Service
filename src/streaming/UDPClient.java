
import java.net.*;

public class UDPClient extends Thread{
    
    public void run(){
        byte[] buf = new byte[256];
        for(int i = 0; i < 256; i++)
            buf[i] = 1;

        InetAddress ip;
        try {
            ip = InetAddress.getByName("127.0.0.1");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }

        DatagramPacket packet = new DatagramPacket(buf, buf.length, ip, 4445);
        try {
            DatagramSocket socket = new DatagramSocket( );
            socket.send(packet);
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
