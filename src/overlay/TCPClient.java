package overlay;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class TCPClient extends Thread{
    private Graph graph;
    private InetAddress neighbor;

    public TCPClient(Graph graph, InetAddress neighbor){
        this.graph = graph;
        this.neighbor = neighbor;
    }

    public void run(){
        try {
            //Socket socket = new Socket("127.0.0.1", 6667);
            Socket socket = new Socket(this.neighbor, 6667);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            MessageSender sender = new MessageSender(out);

            out.println("bruh");
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }
}
