package overlay;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class Vertex {
    private String name;
    private InetAddress ip;
    private Map<String, InetAddress> adjacents;
    private Map<String, Map<String, Double>> routes;

    public Vertex(String name, InetAddress ip){
        this.name = name;
        this.ip = ip;
        this.adjacents = new HashMap<>();
        this.routes = new HashMap<>();
    }

    public Vertex(String name, InetAddress ip, Map<String, InetAddress> adjacents){
        this.name = name;
        this.ip = ip;
        this.adjacents = adjacents;
        this.routes = new HashMap<>();
    }

    public Vertex(String name, InetAddress ip, Map<String, InetAddress> adjacents, Map<String, Map<String, Double>> routes){
        this.name = name;
        this.ip = ip;
        this.adjacents = adjacents;
        this.routes = routes;
    }

    public String getName(){
        return this.name;
    }

    public InetAddress getIp(){
        return this.ip;
    }

    public Map<String, InetAddress> getAdjacents(){
        return this.adjacents;
    }

    public Map<String, Map<String, Double>> getRoutes(){
        return this.routes;
    }

    public void setRoutes(Map<String, Map<String, Double>> routes){
        this.routes = routes;
    }

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
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        
        sb.append("Node: " + this.name + " | IP Address: " + this.ip.toString() + "\n");
        for (Map.Entry<String, InetAddress> entry: this.adjacents.entrySet())
            sb.append("Adjacent: " + entry.getKey() + "\n");

        sb.append(routesToString());

        return sb.toString();
    }

    public String routesToString(){
        StringBuffer sb = new StringBuffer();

        for(Map.Entry<String, Map<String, Double>> entry: routes.entrySet()){
            sb.append("Routes to " + entry.getKey() + ":\n");
            for(Map.Entry<String, Double> entry2: entry.getValue().entrySet()){
                sb.append("\tThrough " + entry2.getKey() + ": " + entry2.getValue() + "\n");
            }
        }

        return sb.toString();
    }
}
