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


public class TCPServer extends Thread{
    private Graph graph;

    public TCPServer(Graph graph){
        this.graph = graph;
    }

    public void run(){
        try{
            ServerSocket server = new ServerSocket(6666);
            
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

        String msg = in.readLine();
        if (msg.equals("hello")){
            String nodeName = graph.getNameFromIP(InetAddress.getByName("127.0.0.3"));//graph.getNameFromIP(client.getInetAddress());
            sendSelfNodeInfo(out, nodeName);
            Map<String, InetAddress> adjs = graph.getNodeAdjacents(nodeName);
            sendAdjacents(out, adjs);
            sendClose(out);
        }
    }

    public void sendSelfNodeInfo(PrintWriter out, String nodeName){
        out.println("YOU: " + nodeName);
    }

    public void sendAdjacents(PrintWriter out, Map<String, InetAddress> adjs){
        for(Map.Entry<String, InetAddress> entry: adjs.entrySet())
            out.println(entry.getKey() + ": " + entry.getValue().getHostAddress());
        out.flush();
    }

    public void sendClose(PrintWriter out){
        out.println("close");
        out.flush();
    }

    public void warnAdjacentsOfClosingNode(List<String> adjs){
        for(String adj: adjs){
            //TODO: warn adjacent
            System.out.println(adj);
        }
    }

}
