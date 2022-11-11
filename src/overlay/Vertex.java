package overlay;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Vertex {
    private String name;
    private List<InetAddress> ipList;
    private Map<String, List<InetAddress>> adjacents;
    private Map<String, Integer> adjsState;
    private int state;

    public static int ON = 1;
    public static int OFF = 2;

    public Vertex(String name, List<InetAddress> ips, int state){
        this.name = name;
        this.ipList = ips;
        this.adjacents = new HashMap<>();
        this.adjsState = null;
        this.state = state;
    }

    public Vertex(String name, Map<String, List<InetAddress>> adjacents, Map<String, Integer> adjsState, int state){
        this.name = name;
        this.ipList = null;
        this.adjacents = adjacents;
        this.adjsState = adjsState;
        this.state = state;
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

    public int getState(){
        return this.state;
    }

    public void setAdjacents(Map<String, List<InetAddress>> adjacents){
        this.adjacents = adjacents;
    }

    public void setState(int state){
        this.state = state;
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

        if (this.state == OFF)
            sb.append("\tState: OFF\n");
        else
            sb.append("\tState: ON\n");

        return sb.toString();
    }
}
