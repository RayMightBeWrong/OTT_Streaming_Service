import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class EchoServer {
    public static void main(String[] args){
        try {
            DatagramSocket socket = new DatagramSocket(4445);
            while(true){
                byte[] buf = new byte[256];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                try {
                    socket.receive(packet);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("received something");
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}
