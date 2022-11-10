package overlay;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Vertex {
    private String name;
    private List<InetAddress> ipList;
    private Map<String, List<InetAddress>> adjacents;

    public Vertex(String name, List<InetAddress> ips){
        this.name = name;
        this.ipList = ips;
        this.adjacents = new HashMap<>();
    }

    public String getName(){
        return this.name;
    }

    public List<InetAddress> getIPList(){
        return this.ipList;
    }

    public Map<String, List<InetAddress>> getAdjacents(){
        return this.adjacents;
    }

    public void setAdjacents(Map<String, List<InetAddress>> adjacents){
        this.adjacents = adjacents;
    }

    /* 
    public Map<String, Map<String, Double>> buildRoutesFromInitialAdjs(){
        Map<String, Map<String, Double>> res = new HashMap<>();
        
        for(Map.Entry<String, InetAddress> adj: this.adjacents.entrySet()){
            Map<String, Double> route = new HashMap<>();
            route.put(adj.getKey(), 0.0);
            res.put(adj.getKey(), route);
        }

        return res;
    }

    public void removeAdjacent(String nodeToRemove){
        this.adjacents.remove(nodeToRemove);
    }*/

    public String toString(){
        StringBuilder sb = new StringBuilder();
        
        sb.append("Node: " + this.name + "\n");
        for (InetAddress ip: this.ipList)
            sb.append("\tAvailable at: " + ip.getHostAddress() + "\n");

        sb.append("\tAdjacents: ");
        for (Map.Entry<String, List<InetAddress>> entry: this.adjacents.entrySet())
            sb.append(entry.getKey() + " ");
        sb.append("\n");

        return sb.toString();
    }
}
