package overlay;

import java.net.InetAddress;
import java.util.Map;

public class Vertex {
    private String name;
    private InetAddress ip;
    private Map<String, InetAddress> adjacents;

    public Vertex(String name, InetAddress ip, Map<String, InetAddress> adjacents){
        this.name = name;
        this.ip = ip;
        this.adjacents = adjacents;
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

    public void removeAdjacent(String nodeToRemove){
        this.adjacents.remove(nodeToRemove);
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        
        sb.append("Node: " + this.name + " | IP Address: " + this.ip.toString() + "\n");
        for (Map.Entry<String, InetAddress> entry: this.adjacents.entrySet())
            sb.append("Adjacent: " + entry.getKey() + "\n");

        return sb.toString();
    }
}
