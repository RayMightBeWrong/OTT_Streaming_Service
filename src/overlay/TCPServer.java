package overlay;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer extends Thread{
    private Graph graph;

    public static int PORT = 6667;

    public TCPServer(Graph graph){
        this.graph = graph;
    }

    public void run(){
        try {
            ServerSocket server = new ServerSocket(PORT);
            
            while(true){
                Socket client = server.accept();
                System.out.println("connected!");
                treatClient(client);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void treatClient(Socket client) throws IOException{
        PrintWriter out = new PrintWriter(client.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        MessageSender sender = new MessageSender(out);

        while(true){
            String msg = in.readLine();
            System.out.println(msg);
        }
    }
}
