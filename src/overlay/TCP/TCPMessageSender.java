package overlay.TCP;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import overlay.state.NodeLink;
import overlay.state.NodeState;
import overlay.state.Vertex;

public class TCPMessageSender {
    private PrintWriter out;

    public TCPMessageSender(PrintWriter out){
        this.out = out;
    }

    public void sendMessage(String msg){
        out.println(msg);
        out.flush();
    }


    /*  BOOTSTRAPPER MESSAGES */

    public void initialMessageBootstrapper(String nodeName, Map<String, List<InetAddress>> adjs, Map<String, Integer> adjsState) throws IOException{
        sendSelfNodeInfo(nodeName);
        sendAdjacents(adjs, adjsState);
        end();
    }

    public void sendSelfNodeInfo(String nodeName){
        String s = "YOU: " + nodeName;
        sendMessage(s);
    }

    public void sendAdjacents(Map<String, List<InetAddress>> adjs, Map<String, Integer> adjsState){
        for(Map.Entry<String, List<InetAddress>> entry: adjs.entrySet()){
            Integer state = adjsState.get(entry.getKey());

            if (state == Vertex.OFF)
                out.println("ADJ: " + entry.getKey() + ": OFF");
                
            else if (state == Vertex.ON)
                out.println("ADJ: " + entry.getKey() + ": ON");

            for(InetAddress ip: entry.getValue())
                out.println("Available at: " + ip.getHostAddress());
        }

        out.flush();
    }


    /*  OTHER MESSAGES */

    public void ack(){
        sendMessage("ack");
    }

    public void hello(){
        sendMessage("hello");
        end();
    }

    public void helloServer(){
        sendMessage("hello");
        sendMessage("i am server");
        end();
    }

    public void probe(boolean initial){
        String msg;
        if(initial)
            msg = "probe: initial: " + LocalDateTime.now();
        else
            msg = "probe: regular: " + LocalDateTime.now();
        
        sendMessage(msg);
    }

    public void sendNewLink(NodeLink link, String self){
        sendMessage("new link: " + link.getDest());
        sendMessage("via node: " + self);
        sendMessage("hops: " + link.getHops());
        sendMessage("cost: " + link.getCost());
        end();
    }

    public void sendRoutes(NodeState state, String receptor){
        sendMessage("routes from: " + state.getSelf());
        
        Map<String, NodeLink> table = state.getTable().getTable();
        for(Map.Entry<String, NodeLink> entry: table.entrySet()){
            if (!receptor.equals(entry.getKey())){
                NodeLink link = entry.getValue();
                sendMessage("link to: " + link.getDest());
                sendMessage("via node: " + state.getSelf());
                sendMessage("hops: " + link.getHops());
                sendMessage("cost: " + link.getCost());
                sendMessage("route done");
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("servers:");
        for(String server: state.getServers())
            sb.append(" " + server);
        sendMessage(sb.toString());

        end();
    }

    public void sendInitialMonitoringMessage(NodeState state){
        sendMessage("monitoring: " + state.getSelf());
        probe(false);
        end();
    }

    public void sendMonitoringMessage(NodeState state, String[] nodesVisited){
        StringBuilder msg = new StringBuilder("monitoring:");
        
        for(String node: nodesVisited)
            msg.append(" " + node);
        msg.append(" " + state.getSelf());

        sendMessage(msg.toString());
        probe(false);
        end();
    }

    public void streamClient(){
        sendMessage("i want a stream");
        end();
    }

    public void sendAskStreaming(NodeState state, String fromServer){
        sendMessage("want streaming: " + state.getSelf());
        sendMessage("from server: " + fromServer);
        end();
    }

    public void sendAskStreaming(NodeState state, String[] args){
        sendMessage("want streaming: " + args[0]);
        sendMessage("from server: " + args[1]);
        end();
    }

    public void sendNewStreamSignal(String[] nodesVisited, String node){
        sendMessage("sending stream to: " + node);

        StringBuilder msg = new StringBuilder("sent to:");
        for(String visited: nodesVisited)
            msg.append(" " + visited);

        sendMessage(msg.toString());
        end();
    }

    public void sendOpenUDPMiddleManSignal(String[] nodesVisited, String dest){
        sendMessage("open UDP middleman: " + dest);

        StringBuilder msg = new StringBuilder("sent to:");
        for(String visited: nodesVisited)
            msg.append(" " + visited);
        
        sendMessage(msg.toString());
        end();
    }

    public void ackOpenUDPMiddleManSignal(String[] nodesVisited){
        sendMessage("ack open UDP middleman");
        StringBuilder msg = new StringBuilder("sent to:");
        for(String visited: nodesVisited)
            msg.append(" " + visited);
        
        sendMessage(msg.toString());
        end();
    }

    public void end(){
        sendMessage("end");
    }
}
