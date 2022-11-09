package overlay;

import java.net.InetAddress;
import java.util.Map;

public class NodeManager {
    public static void main(String[] args){
        if (args.length == 2 && args[0].equals("config")){
            ConfigParser parser = new ConfigParser(args[1]);
            Graph graph = parser.parseXML();
            System.out.println(graph.toString());

            Thread bstrapper = new Thread(new BStrapper(graph));
            bstrapper.start();

            try {
                bstrapper.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        else if (args.length == 1){
            Graph graph = BStrapperClient.readInitialMsg(args[0]);

            Thread server = new Thread(new TCPServer(graph));
            server.start();

            Map<String, InetAddress> adjs = graph.getMyAdjacents();
            for (Map.Entry<String, InetAddress> adj: adjs.entrySet()){
                Thread client = new Thread(new TCPClient(graph, adj.getValue()));
                client.start();
            }

            /* 
            

            try {
                server.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
        }
    }
}
