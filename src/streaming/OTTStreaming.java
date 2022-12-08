package streaming;

import java.net.InetAddress;
import java.net.UnknownHostException;

import overlay.TCPClient;

public class OTTStreaming {

    public static void main(String[] args){
        TCPClient client;
        try {
            client = new TCPClient(null, InetAddress.getByName("localhost"), TCPClient.OPEN_STREAM_CLIENT);
            client.run();
            new Cliente();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
