package overlay;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class TCPServer extends Thread{
    private NodeState state;

    public static final int PORT = 6667;

    public TCPServer(NodeState state){
        this.state = state;
    }

    public void run(){
        try {
            ServerSocket server = new ServerSocket(PORT);

            startInitialClientThreads();

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
        sendProbes(nodeName);
    }
    
    public void readProbe(Socket client, String msg) throws InterruptedException{
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime timestamp = getTimestampFromProbe(msg);
        Duration duration = Duration.between(timestamp, now);

        String nodeName = this.state.findAdjNodeFromAddress(client.getInetAddress());
        this.state.addLink(nodeName, nodeName, client.getInetAddress(), duration.toNanos());

        sendNewLinkToAdjacents(nodeName);
        sendRoutesToNewAdj(nodeName);
    }

    public void readNewLink(Socket client, BufferedReader in, String msg) throws IOException{
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
                if(this.state.isLinkBetter(name, newLink))
                    this.state.addLink(name, newLink);
                break;
            }
        }
    }

    public void readRoutes(BufferedReader in, String msg) throws IOException{
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
                if(this.state.isLinkBetter(dest, newLink))
                    this.state.addLink(dest, newLink);
            }
            else if (isEnd(msg))
                break;
        }
    }



    /* THREAD FUNCTIONS */

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

    public void sendProbes(String key) throws InterruptedException{
        List<InetAddress> ips = this.state.findAddressesFromAdjNode(key);

        Thread client = new Thread(new TCPClient(this.state, ips.get(0), TCPClient.PROBE));
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

    public void sendRoutesToNewAdj(String fromNode) throws InterruptedException{
        List<InetAddress> ips = this.state.findAddressesFromAdjNode(fromNode);

        Thread client = new Thread(new TCPClient(this.state, ips.get(0), TCPClient.SEND_ROUTES, fromNode));
        client.start();
        client.join();
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

    public boolean isNewLink(String msg){
        return isPrefixOf(msg, "new link");
    }

    public boolean isRoutes(String msg){
        return isPrefixOf(msg, "routes from");
    }

    public boolean isEnd(String msg){
        return isPrefixOf(msg, "end");
    }






    public String getSuffixFromPrefix(String msg, String prefix){
        StringBuilder sb = new StringBuilder();
        char[] msgv = msg.toCharArray();

        for(int i = prefix.length(); i < msgv.length; i++)
            sb.append(msgv[i]);

        return sb.toString();
    }

    public LocalDateTime getTimestampFromProbe(String msg){
        String probe = getSuffixFromPrefix(msg, "probe: ");
        return LocalDateTime.parse(probe);
    }

    public String getNewLinkDest(String msg){
        return getSuffixFromPrefix(msg, "new link: ");
    }
}
