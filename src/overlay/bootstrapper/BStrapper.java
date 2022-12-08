package overlay.bootstrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;

import overlay.TCP.TCPMessageSender;
import overlay.state.Graph;
import overlay.state.Vertex;

public class BStrapper extends Thread{
    private Graph graph;
    public static final int PORT = 6666;

    public BStrapper(Graph graph){
        this.graph = graph;
    }

    public void run(){
        try{
            ServerSocket server = new ServerSocket(PORT);
            
            while(true){
                Socket client = server.accept();
                treatClient(client);
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public void treatClient(Socket client) throws IOException{
        PrintWriter out = new PrintWriter(client.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        TCPMessageSender sender = new TCPMessageSender(out);
        
        while(true){
            String msg = in.readLine();
            System.out.println("B: " + msg);

            if (msg.equals("hello")){
                String nodeName = this.graph.getNameFromIP(client.getInetAddress());
                this.graph.setNodeState(nodeName, Vertex.ON);
                Map<String, List<InetAddress>> adjs = this.graph.getNodeAdjacents(nodeName);
                Map<String, Integer> adjsState = this.graph.getNodeAdjacentsState(nodeName);
                sender.initialMessageBootstrapper(nodeName, adjs, adjsState);
            }
            else if (msg.equals("ack"))
                break;
        }

        client.close();
    }
}
