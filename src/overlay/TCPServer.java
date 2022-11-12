package overlay;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
        PrintWriter out = new PrintWriter(client.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        MessageSender sender = new MessageSender(out);

        while(true){
            String msg = in.readLine();
            System.out.println("S: " + msg);

            if (msg.equals("hello")){
                String nodeName = this.state.findAdjNodeFromAddress(client.getInetAddress());
                if (this.state.getAdjState(nodeName) == Vertex.OFF){
                    this.state.setAdjState(nodeName, Vertex.ON);
                    startInitialClientThread(nodeName);
                }
                sendProbes(nodeName);
                sender.end();
                break;
            }
            else if (isProbe(msg)){
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime timestamp = getTimestampFromProbe(msg);
                Duration duration = Duration.between(timestamp, now);

                String nodeName = this.state.findAdjNodeFromAddress(client.getInetAddress());
                this.state.addLink(nodeName, nodeName, client.getInetAddress(), duration.toNanos());

                sendNewLinkToAdjacents(sender, nodeName);
                //sendTopologyToNewAdj(sender, nodeName);
                sender.end();
                break;
            }
            //else if (msg.equals("bruh")){
            //    sender.end();
            //    break;
            //}

        }

        client.close();
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

    public void startInitialClientThread(String key){
        List<InetAddress> ips = this.state.findAddressesFromAdjNode(key);

        Thread client = new Thread(new TCPClient(this.state, ips.get(0), TCPClient.HELLO));
        client.start();
    }

    public void sendProbes(String key){
        List<InetAddress> ips = this.state.findAddressesFromAdjNode(key);

        Thread client = new Thread(new TCPClient(this.state, ips.get(0), TCPClient.PROBE));
        client.start();
    }

    public void sendNewLinkToAdjacents(MessageSender sender, String fromNode) throws InterruptedException{
        Map<String, Integer> adjsState = this.state.getNodeAdjacentsState();
        Map<String, List<InetAddress>> adjs = this.state.getNodeAdjacents();

        for(Map.Entry<String, Integer> entry: adjsState.entrySet()){
            if (entry.getValue() == Vertex.ON){
                if(!entry.getKey().equals(fromNode)){
                    List<InetAddress> ips = adjs.get(entry.getKey());
                    Thread client = new Thread(new TCPClient(this.state, ips.get(0), TCPClient.SEND_NEW_LINK));
                    client.start();
                    client.join();
                }
            }
        }
    }

    public void sendTopologyToNewAdj(MessageSender sender, String fromNode){
        /*
        Map<String, Integer> adjsState = this.state.getNodeAdjacentsState();
        Map<String, List<InetAddress>> adjs = this.state.getNodeAdjacents();

        for(Map.Entry<String, Integer> entry: adjsState.entrySet()){
            if (entry.getValue() == Vertex.ON){
                List<InetAddress> ips = adjs.get(entry.getKey());
                Thread client = new Thread(new TCPClient(this.state, ips.get(0), TCPClient.SEND_NEW_LINK));
                client.start();
            }
        }*/
    }


    public boolean isProbe(String msg){
        char[] ch = msg.toCharArray();
        return ch[0] == 'p' && ch[1] == 'r' && ch[2] == 'o' && ch[3] == 'b' && ch[4] == 'e';
    }

    public LocalDateTime getTimestampFromProbe(String msg){
        StringBuilder sb = new StringBuilder();

        char[] ch = msg.toCharArray();
        for(int i = 7; i < ch.length; i++)
            sb.append(ch[i]);
        
        return LocalDateTime.parse(sb.toString());
    }
}
