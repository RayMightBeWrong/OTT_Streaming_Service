import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class EchoClient {
    public static void main(String[] args){
        byte[] buf = new byte[256];
        for(int i = 0; i < 256; i++)
            buf[i] = 1;
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        try {
            DatagramSocket socket = new DatagramSocket();
            socket.send(packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
