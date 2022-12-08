package overlay.TCP;

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

import overlay.state.NodeLink;
import overlay.state.NodeState;
import overlay.state.Vertex;
import streaming.UDPClient;
import streaming.UDPMiddleMan;
import streaming.UDPServer;


public class TCPHandler {
    private NodeState state;
    private int nodeType;

    private String SERVER = "O1";

    public static final int PORT = 6667;

    public static final int NORMAL_NODE = 1;
    public static final int SERVER_NODE = 2;

    public TCPHandler(NodeState state, int nodeType){
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
            else if (isNewStreamSignal(msg)){
                readNewStreamSignal(in, msg); break;
            }
            else if (isOpenUDPMiddleMan(msg)){
                readOpenUDPMiddleMan(client, msg); break;
            }
            else if (isACKOpenUDPMiddleMan(msg)){
                readACKOpenUDPMiddleMan(client, msg); break;
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

    public void readAskStreaming(BufferedReader in, String msg) throws Exception{
        if(this.state.getSelf().equals(SERVER)){
            String dest = getSuffixFromPrefix(msg, "want streaming: ");
            System.out.println("got it");
            this.state.addStream(dest);

            sendNewStreamSignal(new String[0], dest);
            sendOpenUDPMiddleMan(dest);

            System.out.println("\n__________________________________________________\n\nESTADO");
            System.out.println(this.state.toString());
            System.out.println("__________________________________________________");
        }
        else{
            redirectMessage(SERVER, msg);
        }
    }

    public void readNewStreamSignal(BufferedReader in, String msg) throws Exception{
        String dest = getSuffixFromPrefix(msg, "sending stream to: ");
        this.state.addStream(dest);

        String[] args = {};

        while(true){
            msg = in.readLine();
            
            if(isPrefixOf(msg, "sent to")){
                args = getNodesVisited(msg);
            }
            else if (isEnd(msg))
                break;
        }

        sendNewStreamSignal(args, dest);

        System.out.println("\n__________________________________________________\n\nESTADO");
        System.out.println(this.state.toString());
        System.out.println("__________________________________________________");
    }

    public void readOpenUDPMiddleMan(Socket client, String msg) throws Exception{
        String from = this.state.findAdjNodeFromAddress(client.getInetAddress());
        String dest = getSuffixFromPrefix(msg, "open UDP middleman: ");

        if (!this.state.getSelf().equals(dest)){
            sendOpenUDPMiddleMan(dest);
        }
        startUDPMiddleMan(dest);
        sendACKOpenUDPMiddleMan(from);
    }

    public void readACKOpenUDPMiddleMan(Socket client, String msg) throws Exception{
        String from = this.state.findAdjNodeFromAddress(client.getInetAddress());
        
        if (this.state.getSelf().equals(SERVER))
            startVideoSender("O5");
        //if (this.state.getSelf().equals(SERVER))
        //    ;
        
    }


    /* THREAD FUNCTIONS */

    public void redirectMessage(String dest, String message){
        NodeLink link = this.state.getLinkTo(dest);
        Thread client = new Thread(new TCPCommunicator(this.state, link.getViaInterface(), TCPCommunicator.REDIRECT, message));
        client.run();
    }

    public void startInitialClientThreads() throws InterruptedException{
        Map<String, Integer> adjsState = this.state.getNodeAdjacentsState();
        Map<String, List<InetAddress>> adjs = this.state.getNodeAdjacents();

        for(Map.Entry<String, Integer> entry: adjsState.entrySet()){
            if (entry.getValue() == Vertex.ON){
                List<InetAddress> ips = adjs.get(entry.getKey());
                Thread client = new Thread(new TCPCommunicator(this.state, ips.get(0), TCPCommunicator.HELLO));
                client.start();
                client.join();
            }
        }
    }

    public void startInitialClientThread(String key) throws InterruptedException{
        List<InetAddress> ips = this.state.findAddressesFromAdjNode(key);

        Thread client = new Thread(new TCPCommunicator(this.state, ips.get(0), TCPCommunicator.HELLO));
        client.start();
        client.join();
    }

    public void startUDPServer(){
        //Thread UDPServer = new Thread(new UDPServer());
        //UDPServer.start();
    }

    public void startVideoSender(String dest){
        NodeLink link = this.state.getLinkTo(dest);
        Thread UDPServer = new Thread(new UDPServer(link.getViaInterface()));
        UDPServer.start();
    }

    public void startUDPClient(String dest){
        //NodeLink link = this.state.getLinkTo(dest);
        Thread UDPClient = new Thread(new UDPClient());
        UDPClient.start();
    }

    public void startUDPMiddleMan(String dest){
        if (!this.state.getSelf().equals(dest)){
            NodeLink link = this.state.getLinkTo(dest);
            Thread UDPClient = new Thread(new UDPMiddleMan(link.getViaInterface()));
            UDPClient.start();
        }
        else{
            Thread UDPClient = new Thread(new UDPClient());
            UDPClient.start();
        }
    }

    public void startMonitoring(){
        Timer timer = new Timer();
        timer.schedule(new TCPMonitor(state), 0, 3000);
    }

    public void sendStreamRequest(){
        NodeLink link = this.state.getLinkTo(SERVER);
        Thread client = new Thread(new TCPCommunicator(this.state, link.getViaInterface(), TCPCommunicator.ASK_STREAMING));
        client.run();
    }

    public void sendProbe(String key, boolean initial) throws InterruptedException{
        List<InetAddress> ips = this.state.findAddressesFromAdjNode(key);

        Thread client;
        if (initial)
            client = new Thread(new TCPCommunicator(this.state, ips.get(0), TCPCommunicator.PROBE_INITIAL));
        else
            client = new Thread(new TCPCommunicator(this.state, ips.get(0), TCPCommunicator.PROBE_REGULAR));

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
                    Thread client = new Thread(new TCPCommunicator(this.state, ips.get(0), TCPCommunicator.SEND_NEW_LINK, fromNode));
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
                    Thread client = new Thread(new TCPCommunicator(this.state, ips.get(0), TCPCommunicator.SEND_NEW_LINK, fromNode));
                    client.start();
                    client.join();
                }
            }
        }
    }

    public void sendRoutesToNewAdj(String fromNode) throws InterruptedException{
        List<InetAddress> ips = this.state.findAddressesFromAdjNode(fromNode);

        Thread client = new Thread(new TCPCommunicator(this.state, ips.get(0), TCPCommunicator.SEND_ROUTES, fromNode));
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
                    Thread client = new Thread(new TCPCommunicator(this.state, ips.get(0), TCPCommunicator.MONITORING, nodesVisited));
                    client.start();
                    client.join();
                }
            }
        }
    }

    public void sendNewStreamSignal(String[] nodesVisited, String dest) throws InterruptedException{
        Map<String, Integer> adjsState = this.state.getNodeAdjacentsState();
        Map<String, List<InetAddress>> adjs = this.state.getNodeAdjacents();

        String[] nodesInfo = new String[nodesVisited.length + 2];
        for(int i = 0; i < nodesVisited.length; i++)
            nodesInfo[i] = nodesVisited[i];
        nodesInfo[nodesVisited.length] = this.state.getSelf();
        nodesInfo[nodesVisited.length + 1] = dest;


        for(Map.Entry<String, Integer> entry: adjsState.entrySet()){
            if (entry.getValue() == Vertex.ON){
                if(!isNodeInArray(nodesVisited, entry.getKey())){
                    List<InetAddress> ips = adjs.get(entry.getKey());
                    Thread client = new Thread(new TCPCommunicator(this.state, ips.get(0), TCPCommunicator.NEW_STREAM, nodesInfo));
                    client.start();
                    client.join();
                }
            }
        }
    }

    public void sendOpenUDPMiddleMan(String dest){
        NodeLink link = this.state.getLinkTo(dest);
        Thread client = new Thread(new TCPCommunicator(this.state, link.getViaInterface(), TCPCommunicator.OPEN_UDP_MIDDLEMAN, dest));
        client.start();
    }

    public void sendACKOpenUDPMiddleMan(String dest){
        NodeLink link = this.state.getLinkTo(dest);
        Thread client = new Thread(new TCPCommunicator(this.state, link.getViaInterface(), TCPCommunicator.ACK_OPEN_UDP_MIDDLEMAN));
        client.start();
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

    public boolean isNewStreamSignal(String msg){
        return isPrefixOf(msg, "sending stream to");
    }

    public boolean isOpenUDPMiddleMan(String msg){
        return isPrefixOf(msg, "open UDP middleman");
    }

    public boolean isACKOpenUDPMiddleMan(String msg){
        return isPrefixOf(msg, "ack open UDP middleman");
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
