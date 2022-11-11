package overlay;

import java.net.InetAddress;

public class NodeLink {
    private String dest;
    private String viaNode;
    private InetAddress viaInterface;
    private int hops;
    private double cost;

    public NodeLink(String dest, String viaNode, InetAddress viaInterface, int hops, double cost){
        this.dest = dest;
        this.viaNode = viaNode;
        this.viaInterface = viaInterface;
        this.hops = hops;
        this.cost = cost;
    }

    // creating adjacent
    public NodeLink(String dest, String viaNode){
        this.dest = dest;
        this.viaNode = viaNode;
        this.viaInterface = null;
        this.hops = 1;
        this.cost = -1;
    }
}
