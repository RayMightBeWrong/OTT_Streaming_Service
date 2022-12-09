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
import streaming.UDP.UDPClient;
import streaming.UDP.UDPMiddleMan;
import streaming.UDP.UDPServer;


public class TCPHandler {
    private NodeState state;
    private int nodeType;

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
                this.state.addServer(this.state.getSelf());
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
                System.out.println("S: hello\n");
                readHello(client, in, msg); break;
            }
            else if (isProbe(msg)){
                System.out.println("S: probe\n");
                readProbe(client, msg); break;
            }
            else if (isNewLink(msg)){
                System.out.println("S: new link\n");
                readNewLink(client, in, msg); break;
            }
            else if (isRoutes(msg)){
                System.out.println("S: routes\n");
                readRoutes(in, msg); break;
            }
            else if (isMonitoring(msg)){
                System.out.println("S: monitoring\n");
                readMonitoring(client, in, msg); break;
            }
            else if (isStreamClient(msg)){
                System.out.println("S: client wants stream\n");
                sendStreamRequest(); break;
            }
            else if (isAskStreaming(msg)){
                System.out.println("S: stream request\n");
                readAskStreaming(in, msg); break;
            }
            else if (isNewStreamSignal(msg)){
                System.out.println("S: new stream\n");
                readNewStreamSignal(in, msg); break;
            }
            else if (isOpenUDPMiddleMan(msg)){
                System.out.println("S: open UDP middleman\n");
                readOpenUDPMiddleMan(client, in, msg); break;
            }
            else if (isACKOpenUDPMiddleMan(msg)){
                System.out.println("S: ack open UDP middleman\n");
                readACKOpenUDPMiddleMan(client, in, msg); break;
            }
        }

        client.close();
    }


    /* READ FUNCTIONS */

    public void readHello(Socket client, BufferedReader in, String msg) throws Exception{
        String nodeName = this.state.findAdjNodeFromAddress(client.getInetAddress());
        boolean isServer = false;

        while(true){
            msg = in.readLine();

            if (isPrefixOf(msg, "i am server")){
                isServer = true;
            }
            else if (isEnd(msg)){
                if (this.state.getAdjState(nodeName) == Vertex.OFF){
                    this.state.setAdjState(nodeName, Vertex.ON);
                    if (isServer)
                        this.state.addServer(nodeName);
                    startInitialClientThread(nodeName);
                }
                sendProbe(nodeName, true);
                break;
            }
        }
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
            else if (isPrefixOf(msg, "servers")){
                String[] servers = getServers(msg);
                for(String server: servers)
                    this.state.addServer(server);
            }
            else if (isEnd(msg))
                break;
        }

        System.out.println("\n__________________________________________________\n\nESTADO");
        System.out.println(this.state.toString());
        System.out.println("__________________________________________________");
    }

    public void readMonitoring(Socket client, BufferedReader in, String msg) throws Exception{
        String[] args = getNodesVisited(msg, "monitoring: ");

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
        String server = "";
        String dest = getSuffixFromPrefix(msg, "want streaming: ");

        while(true){
            msg = in.readLine();

            if (isPrefixOf(msg, "from server")){
                server = getSuffixFromPrefix(msg, "from server: ");
            }
            else if (isEnd(msg))
                break;
        }

        if(this.state.getSelf().equals(server)){
            this.state.addStream(dest);

            sendNewStreamSignal(new String[0], dest);
            sendOpenUDPMiddleMan(new String[0], dest);
        }
        else{
            sendStreamRequest(dest, server);
        }
    }

    public void readNewStreamSignal(BufferedReader in, String msg) throws Exception{
        String dest = getSuffixFromPrefix(msg, "sending stream to: ");
        this.state.addStream(dest);

        String[] args = {};

        while(true){
            msg = in.readLine();
            
            if(isPrefixOf(msg, "sent to")){
                args = getNodesVisited(msg, "sent to: ");
            }
            else if (isEnd(msg))
                break;
        }

        sendNewStreamSignal(args, dest);
    }

    public void readOpenUDPMiddleMan(Socket client, BufferedReader in, String msg) throws Exception{
        String dest = getSuffixFromPrefix(msg, "open UDP middleman: ");
        
        String[] args = {};
        while(true){
            msg = in.readLine();
            
            if(isPrefixOf(msg, "sent to")){
                args = getNodesVisited(msg, "sent to: ");
            }
            else if (isEnd(msg))
                break;
        }

        if (!this.state.getSelf().equals(dest)){
            sendOpenUDPMiddleMan(args, dest);
        }
        else{
            sendACKOpenUDPMiddleMan(args);
        }

        startUDPMiddleMan(dest);
    }

    public void readACKOpenUDPMiddleMan(Socket client, BufferedReader in, String msg) throws Exception{
        String[] args = {};

        while(true){
            msg = in.readLine();
            
            if(isPrefixOf(msg, "sent to")){
                args = getNodesVisited(msg, "sent to: ");
            }
            else if (isEnd(msg))
                break;
        }

        if (this.state.getSelf().equals(args[0])){
            String from = this.state.findAdjNodeFromAddress(client.getInetAddress());
            System.out.println("reached servidor");
            startVideoSender(from);
        }
        else{
            String[] newArgs = new String[args.length - 1];
            for(int i = 0; i < args.length - 1; i++)
                newArgs[i] = args[i];
            sendACKOpenUDPMiddleMan(newArgs);
        }
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
                
                Thread client;
                if (this.nodeType == TCPHandler.NORMAL_NODE)
                    client = new Thread(new TCPCommunicator(this.state, ips.get(0), TCPCommunicator.HELLO));
                else
                    client = new Thread(new TCPCommunicator(this.state, ips.get(0), TCPCommunicator.HELLO_SERVER));
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

    public void startVideoSender(String dest){
        NodeLink link = this.state.getLinkTo(dest);
        Thread UDPServer = new Thread(new UDPServer(link.getViaInterface()));
        UDPServer.start();
    }

    public void startUDPClient(){
        List<InetAddress> ips = this.state.getSelfIPs();

        Thread UDPClient = new Thread(new UDPClient(ips.get(0)));
        UDPClient.start();
    }

    public void startUDPMiddleMan(String dest){
        if (!this.state.getSelf().equals(dest)){
            NodeLink link = this.state.getLinkTo(dest);
            System.out.println("dest: " + dest);
            Thread middleman = new Thread(new UDPMiddleMan(link.getViaInterface()));
            middleman.start();
        }
        else{
            List<InetAddress> ips = this.state.getSelfIPs();

            Thread UDPClient = new Thread(new UDPClient(ips.get(0)));
            UDPClient.start();
        }
    }

    public void startMonitoring(){
        Timer timer = new Timer();
        timer.schedule(new TCPMonitor(state), 0, 3000);
    }

    public void sendStreamRequest(){
        NodeLink link = this.state.getClosestServer();
        Thread client = new Thread(new TCPCommunicator(this.state, link.getViaInterface(), TCPCommunicator.ASK_STREAMING, link.getDest()));
        client.run();
    }

    public void sendStreamRequest(String dest, String server){
        NodeLink link = this.state.getLinkTo(server);
        String[] args = {dest, server};
        Thread client = new Thread(new TCPCommunicator(this.state, link.getViaInterface(), TCPCommunicator.REDIRECT_ASK_STREAMING, args));
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

    public void sendOpenUDPMiddleMan(String[] nodesVisited, String dest){
        String[] nodesInfo = new String[nodesVisited.length + 2];
        for(int i = 0; i < nodesVisited.length; i++)
            nodesInfo[i] = nodesVisited[i];
        
        nodesInfo[nodesVisited.length] = this.state.getSelf();
        nodesInfo[nodesVisited.length + 1] = dest;

        NodeLink link = this.state.getLinkTo(dest);
        Thread client = new Thread(new TCPCommunicator(this.state, link.getViaInterface(), TCPCommunicator.OPEN_UDP_MIDDLEMAN, nodesInfo));
        client.start();
    }

    public void sendACKOpenUDPMiddleMan(String[] args){
        String dest = args[args.length - 1];
        NodeLink link = this.state.getLinkTo(dest);
        Thread client = new Thread(new TCPCommunicator(this.state, link.getViaInterface(), TCPCommunicator.ACK_OPEN_UDP_MIDDLEMAN, args));
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

    public String[] getNodesVisited(String msg, String prefix){
        String nodes = getSuffixFromPrefix(msg, prefix);
        return nodes.split(" ");
    }

    public String[] getServers(String msg){
        String nodes = getSuffixFromPrefix(msg, "servers: ");
        return nodes.split(" ");
    }
}
