package overlay;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Timer;

public class TCPServer {
    private NodeState state;
    private int nodeType;

    public static final int PORT = 6667;

    public static final int NORMAL_NODE = 1;
    public static final int SERVER_NODE = 2;

    public TCPServer(NodeState state, int nodeType){
        this.state = state;
        this.nodeType = nodeType;
    }

    public void run(){
        try {
            ServerSocket server = new ServerSocket(PORT);

            startInitialClientThreads();

            if (this.nodeType == SERVER_NODE){
                startMonitoring();
            }

            while(true){
                Socket client = server.accept();
                treatClient(client);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void treatClient(Socket client) throws Exception{
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

        while(true){
            String msg = in.readLine();
            System.out.println("S: " + msg);

            if (isHello(msg)){
                readHello(client, msg); break;
            }
            else if (isProbe(msg)){
                readProbe(client, msg); break;
            }
            else if (isNewLink(msg)){
                readNewLink(client, in, msg); break;
            }
            else if (isRoutes(msg)){
                readRoutes(in, msg); break;
            }
            else if (isMonitoring(msg)){
                readMonitoring(client, in, msg); break;
            }
            else if (isStreamClient(msg)){
                sendStreamRequest(); break;
            }
            else if (isAskStreaming(msg)){
                readAskStreaming(in, msg); break;
            }
        }

        client.close();
    }


    /* READ FUNCTIONS */

    public void readHello(Socket client, String msg) throws InterruptedException{
        String nodeName = this.state.findAdjNodeFromAddress(client.getInetAddress());
        if (this.state.getAdjState(nodeName) == Vertex.OFF){
            this.state.setAdjState(nodeName, Vertex.ON);
            startInitialClientThread(nodeName);
        }
        sendProbe(nodeName, true);
    }
    
    public void readProbe(Socket client, String msg) throws InterruptedException{
        boolean initialMsg = isProbeInitial(msg);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime timestamp = getTimestampFromProbe(msg, initialMsg);
        Duration duration = Duration.between(timestamp, now);

        String nodeName = this.state.findAdjNodeFromAddress(client.getInetAddress());
        NodeLink link = new NodeLink(nodeName, nodeName, client.getInetAddress(), Math.abs(duration.toNanos()));
        if(this.state.isLinkModified(nodeName, link)){
            this.state.addLink(nodeName, link);
            sendNewLinkToAdjacents(nodeName);

            System.out.println("\n__________________________________________________\n\nESTADO");
            System.out.println(this.state.toString());
            System.out.println("__________________________________________________");
        }
        if(initialMsg)
            sendRoutesToNewAdj(nodeName);
    }

    public void readNewLink(Socket client, BufferedReader in, String msg) throws Exception{
        String name = getNewLinkDest(msg);
        String viaNode = "";
        InetAddress viaInterface = null;
        int hops = 0;
        long cost = 0;

        while(true){
            msg = in.readLine();

            if (isPrefixOf(msg, "via node")){
                viaNode = getSuffixFromPrefix(msg, "via node: ");
            }
            else if (isPrefixOf(msg, "hops")){
                String hopsString = getSuffixFromPrefix(msg, "hops: ");
                hops = Integer.parseInt(hopsString) + 1;
            }
            else if (isPrefixOf(msg, "cost")){
                String costString = getSuffixFromPrefix(msg, "cost: ");
                cost = Long.parseLong(costString);
                NodeLink adj = this.state.getLinkTo(viaNode);
                cost += adj.getCost();
            }
            else if (isEnd(msg)){
                List<InetAddress> ips = this.state.findAddressesFromAdjNode(viaNode);
                viaInterface = ips.get(0);
                NodeLink newLink = new NodeLink(name, viaNode, viaInterface, hops, cost);
                if(this.state.isLinkModified(name, newLink)){
                    this.state.addLink(name, newLink);
                    sendNewLinkToAdjacents(name, viaNode);

                    System.out.println("\n__________________________________________________\n\nESTADO");
                    System.out.println(this.state.toString());
                    System.out.println("__________________________________________________");
                }
                else
                    System.out.println("new link refused");

                break;
            }
        }
    }

    public void readRoutes(BufferedReader in, String msg) throws Exception{
        String dest = "";
        String viaNode = "";
        InetAddress viaInterface = null;
        int hops = 0;
        long cost = 0;

        while(true){
            msg = in.readLine();

            if (isPrefixOf(msg, "link to")){
                dest = getSuffixFromPrefix(msg, "link to: ");
            }
            else if (isPrefixOf(msg, "via node")){
                viaNode = getSuffixFromPrefix(msg, "via node: ");
            }
            else if (isPrefixOf(msg, "hops")){
                String hopsString = getSuffixFromPrefix(msg, "hops: ");
                hops = Integer.parseInt(hopsString) + 1;
            }
            else if (isPrefixOf(msg, "cost")){
                String costString = getSuffixFromPrefix(msg, "cost: ");
                cost = Long.parseLong(costString);
                NodeLink adj = this.state.getLinkTo(viaNode);
                cost += adj.getCost();
            }
            else if (isPrefixOf(msg, "route done")){
                List<InetAddress> ips = this.state.findAddressesFromAdjNode(viaNode);
                viaInterface = ips.get(0);
                NodeLink newLink = new NodeLink(dest, viaNode, viaInterface, hops, cost);
                if(this.state.isLinkModified(dest, newLink)){
                    this.state.addLink(dest, newLink);
                    sendNewLinkToAdjacents(dest, viaNode);
                }
            }
            else if (isEnd(msg))
                break;
        }

        System.out.println("\n__________________________________________________\n\nESTADO");
        System.out.println(this.state.toString());
        System.out.println("__________________________________________________");
    }

    public void readMonitoring(Socket client, BufferedReader in, String msg) throws Exception{
        String[] args = getNodesVisited(msg);

        String fromNode = args[args.length - 1];
        sendProbe(fromNode, false);
        sendMonitoringToAdjacents(args);

        while(true){
            msg = in.readLine();
            
            if(isProbe(msg)){
                readProbe(client, msg);
            }
            else if (isEnd(msg))
                break;
        }
    }

    public void readAskStreaming(BufferedReader in, String msg){
        if(this.state.getSelf().equals("O1")){
            System.out.println("got it");
        }
        else{
            redirectMessage("O1", msg);
        }
    }


    /* THREAD FUNCTIONS */

    public void redirectMessage(String dest, String message){
        NodeLink link = this.state.getLinkTo(dest);
        Thread client = new Thread(new TCPClient(this.state, link.getViaInterface(), TCPClient.REDIRECT, message));
        client.run();
    }

    public void startInitialClientThreads() throws InterruptedException{
        Map<String, Integer> adjsState = this.state.getNodeAdjacentsState();
        Map<String, List<InetAddress>> adjs = this.state.getNodeAdjacents();

        for(Map.Entry<String, Integer> entry: adjsState.entrySet()){
            if (entry.getValue() == Vertex.ON){
                List<InetAddress> ips = adjs.get(entry.getKey());
                Thread client = new Thread(new TCPClient(this.state, ips.get(0), TCPClient.HELLO));
                client.start();
                client.join();
            }
        }
    }

    public void startInitialClientThread(String key) throws InterruptedException{
        List<InetAddress> ips = this.state.findAddressesFromAdjNode(key);

        Thread client = new Thread(new TCPClient(this.state, ips.get(0), TCPClient.HELLO));
        client.start();
        client.join();
    }

    public void startMonitoring(){
        Timer timer = new Timer();
        timer.schedule(new TCPMonitorClient(state), 0, 3000);
    }

    public void sendStreamRequest(){
        NodeLink link = this.state.getLinkTo("O1");
        Thread client = new Thread(new TCPClient(this.state, link.getViaInterface(), TCPClient.ASK_STREAMING));
        client.run();
    }

    public void sendProbe(String key, boolean initial) throws InterruptedException{
        List<InetAddress> ips = this.state.findAddressesFromAdjNode(key);

        Thread client;
        if (initial)
            client = new Thread(new TCPClient(this.state, ips.get(0), TCPClient.PROBE_INITIAL));
        else
            client = new Thread(new TCPClient(this.state, ips.get(0), TCPClient.PROBE_REGULAR));

        client.start();
        client.join();
    }

    public void sendNewLinkToAdjacents(String fromNode) throws InterruptedException{
        Map<String, Integer> adjsState = this.state.getNodeAdjacentsState();
        Map<String, List<InetAddress>> adjs = this.state.getNodeAdjacents();

        for(Map.Entry<String, Integer> entry: adjsState.entrySet()){
            if (entry.getValue() == Vertex.ON){
                if(!entry.getKey().equals(fromNode)){
                    List<InetAddress> ips = adjs.get(entry.getKey());
                    Thread client = new Thread(new TCPClient(this.state, ips.get(0), TCPClient.SEND_NEW_LINK, fromNode));
                    client.start();
                    client.join();
                }
            }
        }
    }

    public void sendNewLinkToAdjacents(String fromNode, String viaNode) throws InterruptedException{
        Map<String, Integer> adjsState = this.state.getNodeAdjacentsState();
        Map<String, List<InetAddress>> adjs = this.state.getNodeAdjacents();

        for(Map.Entry<String, Integer> entry: adjsState.entrySet()){
            if (entry.getValue() == Vertex.ON){
                if(!entry.getKey().equals(fromNode) && !entry.getKey().equals(viaNode)){
                    List<InetAddress> ips = adjs.get(entry.getKey());
                    Thread client = new Thread(new TCPClient(this.state, ips.get(0), TCPClient.SEND_NEW_LINK, fromNode));
                    client.start();
                    client.join();
                }
            }
        }
    }

    public void sendRoutesToNewAdj(String fromNode) throws InterruptedException{
        List<InetAddress> ips = this.state.findAddressesFromAdjNode(fromNode);

        Thread client = new Thread(new TCPClient(this.state, ips.get(0), TCPClient.SEND_ROUTES, fromNode));
        client.start();
        client.join();
    }

    public void sendMonitoringToAdjacents(String[] nodesVisited) throws InterruptedException{
        Map<String, Integer> adjsState = this.state.getNodeAdjacentsState();
        Map<String, List<InetAddress>> adjs = this.state.getNodeAdjacents();

        for(Map.Entry<String, Integer> entry: adjsState.entrySet()){
            if (entry.getValue() == Vertex.ON){
                if(!isNodeInArray(nodesVisited, entry.getKey())){
                    List<InetAddress> ips = adjs.get(entry.getKey());
                    Thread client = new Thread(new TCPClient(this.state, ips.get(0), TCPClient.MONITORING, nodesVisited));
                    client.start();
                    client.join();
                }
            }
        }
    }


    /* AUXILIARY READ FUNCTIONS */

    public boolean isPrefixOf(String msg, String prefix){
        boolean res = true;
        char[] msgv = msg.toCharArray();
        char[] pv = prefix.toCharArray();

        for(int i = 0; i < pv.length && i < msgv.length && res; i++)
            if (pv[i] != msgv[i])
                res = false;

        return res;
    }

    public boolean isHello(String msg){
        return isPrefixOf(msg, "hello");
    }

    public boolean isProbe(String msg){
        return isPrefixOf(msg, "probe");
    }

    public boolean isProbeInitial(String msg){
        return isPrefixOf(msg, "probe: initial");
    }

    public boolean isNewLink(String msg){
        return isPrefixOf(msg, "new link");
    }

    public boolean isRoutes(String msg){
        return isPrefixOf(msg, "routes from");
    }

    public boolean isMonitoring(String msg){
        return isPrefixOf(msg, "monitoring");
    }

    public boolean isStreamClient(String msg){
        return isPrefixOf(msg, "i want a stream");
    }

    public boolean isAskStreaming(String msg){
        return isPrefixOf(msg, "want streaming");
    }

    public boolean isEnd(String msg){
        return isPrefixOf(msg, "end");
    }

    public boolean isNodeInArray(String[] nodes, String node){
        boolean res = false;

        for(String s: nodes)
            if (node.equals(s)){
                res = true;
                break;
            }

        return res;
    }


    public String getSuffixFromPrefix(String msg, String prefix){
        StringBuilder sb = new StringBuilder();
        char[] msgv = msg.toCharArray();

        for(int i = prefix.length(); i < msgv.length; i++)
            sb.append(msgv[i]);

        return sb.toString();
    }

    public LocalDateTime getTimestampFromProbe(String msg, boolean initial){
        String probe;

        if (initial)
            probe = getSuffixFromPrefix(msg, "probe: initial: ");
        else
            probe = getSuffixFromPrefix(msg, "probe: regular: ");

        return LocalDateTime.parse(probe);
    }

    public String getNewLinkDest(String msg){
        return getSuffixFromPrefix(msg, "new link: ");
    }

    public String[] getNodesVisited(String msg){
        String nodes = getSuffixFromPrefix(msg, "monitoring: ");
        return nodes.split(" ");
    }
}
