package overlay;

import java.net.InetAddress;
import java.util.List;

public class Vertex {
    private String name;
    private InetAddress ip;
    private List<String> adjacents;

    public Vertex(String name, InetAddress ip, List<String> adjacents){
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

    public List<String> getAdjacents(){
        return this.adjacents;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        
        sb.append("Node: " + this.name + " | IP Address: " + this.ip.toString() + "\n");
        for (int i = 0; i < this.adjacents.size(); i++)
            sb.append("Adjacent: " + this.adjacents.get(i) + "\n");

        return sb.toString();
    }
}
