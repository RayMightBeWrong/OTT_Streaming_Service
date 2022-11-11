package overlay;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class BStrapper extends Thread{
    private Graph graph;
    public static int PORT = 6666;

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
        MessageSender sender = new MessageSender(out);
        
        while(true){
            String msg = in.readLine();
            //System.out.println(msg);

            if (msg.equals("hello")){
                String nodeName = this.graph.getNameFromIP(client.getInetAddress());
                this.graph.setNodeState(nodeName, Vertex.ON);
                Map<String, List<InetAddress>> adjs = this.graph.getNodeAdjacents(nodeName);
                Map<String, Integer> adjsState = this.graph.getNodeAdjacentsState(nodeName);
                sender.initialMessageBootstrapper(nodeName, adjs, adjsState);
                break;
            }
        }

        client.close();
    }
}
