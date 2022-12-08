package overlay;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class MessageSender {
    private PrintWriter out;

    public MessageSender(PrintWriter out){
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


    
    /*  CLIENT MESSAGES */

    public void hello(){
        sendMessage("hello");
    }


    /*  MESSAGES SENT BY ALL */

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

    public void sendAskStreaming(NodeState state){
        sendMessage("want streaming: " + state.getSelf());
    }

    public void sendNewStreamSignal(NodeState state, String[] nodesVisited, String node){
        sendMessage("sending stream to: " + node);

        StringBuilder msg = new StringBuilder("sent to:");
        for(String visited: nodesVisited)
            msg.append(" " + visited);
        msg.append(" " + state.getSelf());

        sendMessage(msg.toString());
        end();
    }

    public void sendOpenUDPMiddleManSignal(String dest){
        sendMessage("open UDP middleman: " + dest);
    }

    public void ackOpenUDPMiddleManSignal(){
        sendMessage("ack open UDP middleman");
    }

    public void end(){
        sendMessage("end");
    }
}
