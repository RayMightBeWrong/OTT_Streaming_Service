package overlay;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NodeManager {
    public static void main(String[] args){
        if (args.length == 2 && args[0].equals("config")){
            ConfigParser parser = new ConfigParser(args[1]);
            Graph graph = parser.parseXML();

            Thread bstrapper = new Thread(new BStrapper(graph));
            bstrapper.start();

            NodeState state = graph.graphToNodeState(parser.getBootstrapperName());

            TCPServer server = new TCPServer(state, TCPServer.SERVER_NODE);
            server.run();

            try {
                bstrapper.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        else if (args.length == 1){
            if (args[0].equals("stream")){
                TCPClient client;
                try {
                    client = new TCPClient(null, InetAddress.getByName("localhost"), TCPClient.OPEN_STREAM_CLIENT);
                    client.run();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
            else{
                NodeState state = BStrapperClient.readInitialMsg(args[0]);

                TCPServer server = new TCPServer(state, TCPServer.NORMAL_NODE);
                server.run();
            }
        }
    }
}
