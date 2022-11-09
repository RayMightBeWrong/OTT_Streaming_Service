package overlay;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
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
            System.out.println(msg);

            if (msg.equals("hello")){
                String nodeName = this.graph.getNameFromIP(client.getInetAddress());
                //graph.getNameFromIP(client.getInetAddress());
                System.out.println(nodeName);
                Map<String, InetAddress> adjs = this.graph.getNodeAdjacents(nodeName);
                sender.initialMessageBootstrapper(nodeName, adjs);
                break;
            }
            else if (lostNode(msg)){
                String nodeName = getLostNode(msg);
                Map<String, InetAddress> adjs = this.graph.getNodeAdjacents(nodeName);
                this.graph.deleteNode(nodeName);
                warnAdjacentsOfClosingNode(sender, nodeName, adjs);
            }
        }

        client.close();
    }

    public void warnAdjacentsOfClosingNode(MessageSender sender, String nodeName, Map<String, InetAddress> adjs){
        for(Map.Entry<String, InetAddress> adj: adjs.entrySet()){
            sender.nodeClosed(nodeName);
        }
    }

    public boolean lostNode(String msg){
        char[] ch = msg.toCharArray();
        return ch[0] == 'l' && ch[1] == 'o' && ch[2] == 's' && ch[3] == 't';
    }

    public String getLostNode(String msg){
        StringBuilder sb = new StringBuilder();

        char[] ch = msg.toCharArray();
        for(int i = 5; i < ch.length; i++){
            sb.append(ch[i]);
        }
        return sb.toString();
    }
}
