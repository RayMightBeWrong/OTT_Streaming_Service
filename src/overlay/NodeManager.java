package overlay;


public class NodeManager {
    public static void main(String[] args){
        if (args.length == 2 && args[0].equals("config")){
            ConfigParser parser = new ConfigParser(args[1]);
            Graph graph = parser.parseXML();

            Thread bstrapper = new Thread(new BStrapper(graph));
            bstrapper.start();

            try {
                bstrapper.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        else if (args.length == 1){
            NodeState state = BStrapperClient.readInitialMsg(args[0]);

            Thread server = new Thread(new TCPServer(state));
            server.start();
            try {
                server.join();
            } catch (InterruptedException e) {
                // e.printStackTrace();
                // ignore
            }
        }
    }
}
