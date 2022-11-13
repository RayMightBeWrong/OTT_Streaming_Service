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

            if (msg.equals("hello")){
                readHello(client, msg); break;
            }
            else if (isProbe(msg)){
                readProbe(client, msg); break;
            }
            else if (isNewLink(msg)){
                readNewLink(client, in, msg); break;
            }
            else
                break;
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
        sendTopologyToNewAdj(nodeName);
    }

    public void readNewLink(Socket client, BufferedReader in, String msg) throws IOException{
        System.out.println(getNewLinkDest(msg));
        while(true){
            msg = in.readLine();
            System.out.println(msg);

            if (msg.equals("end"))
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

    public void sendTopologyToNewAdj(String fromNode) throws InterruptedException{
        List<InetAddress> ips = this.state.findAddressesFromAdjNode(fromNode);

        Thread client = new Thread(new TCPClient(this.state, ips.get(0), TCPClient.SEND_TOPOLOGY));
        client.start();
        client.join();
    }



    /* AUXILIARY READ FUNCTIONS */

    public boolean isPrefixOf(String msg, String prefix){
        boolean res = true;
        char[] msgv = msg.toCharArray();
        char[] pv = prefix.toCharArray();

        for(int i = 0; i < pv.length && res; i++)
            if (pv[i] != msgv[i])
                res = false;

        return res;
    }

    public boolean isProbe(String msg){
        return isPrefixOf(msg, "probe");
    }

    public boolean isNewLink(String msg){
        return isPrefixOf(msg, "new link");
    }

    public String getSuffixFromPrefix(String msg, String prefix){
        StringBuilder sb = new StringBuilder();
        char[] msgv = msg.toCharArray();

        for(int i = prefix.length(); i < msgv.length; i++)
            sb.append(msgv[i]);

        return sb.toString();
    }

    public LocalDateTime getTimestampFromProbe(String msg){
        StringBuilder sb = new StringBuilder();

        char[] ch = msg.toCharArray();
        for(int i = 7; i < ch.length; i++)
            sb.append(ch[i]);
        
        return LocalDateTime.parse(sb.toString());
    }

    public String getNewLinkDest(String msg){
        return getSuffixFromPrefix(msg, "new link: ");
    }
}
