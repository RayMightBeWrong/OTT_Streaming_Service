package overlay;


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
            NodeState state = BStrapperClient.readInitialMsg(args[0]);

            TCPServer server = new TCPServer(state, TCPServer.NORMAL_NODE);
            server.run();
        }
    }
}
