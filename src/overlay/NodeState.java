package overlay;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class NodeState {
    private Vertex node;
    private DistancesTable table;
    private final ReentrantLock lock;
    
    public NodeState(Vertex node){
        this.node = node;
        this.table = new DistancesTable();
        this.lock = new ReentrantLock();
    }

    public DistancesTable getTable(){
        return this.table;
    }

    public String getSelf(){
        return this.node.getName();
    }

    public void addLink(String dest, NodeLink newLink){
        this.lock.lock();
        try{
            this.table.addLink(dest, newLink);
        }
        finally{
            this.lock.unlock();
        }
    }

    public void addLink(String dest, String viaNode, InetAddress viaInterface, long cost){
        this.lock.lock();
        try{
            this.table.addLink(dest, viaNode, viaInterface, cost);
        }
        finally{
            this.lock.unlock();
        }
    }

    public void setAdjState(String adj, int state){
        this.lock.lock();
        try{
            this.node.setAdjState(adj, state);
        }
        finally{
            this.lock.unlock();
        }
    }

    public Map<String, List<InetAddress>> getNodeAdjacents(){
        return this.node.getAdjacents();
    }

    public Map<String, Integer> getNodeAdjacentsState(){
        return this.node.getAdjacentsState();
    }

    public int getAdjState(String key){
        return this.node.getAdjState(key);
    }

    public List<InetAddress> findAddressesFromAdjNode(String key){
        return this.node.findAddressesFromNode(key);
    }

    public String findAdjNodeFromAddress(InetAddress ip){
        return this.node.findNodeFromAddress(ip);
    }

    public NodeLink getLinkTo(String key){
        return this.table.getLinkTo(key);
    }

    public boolean isLinkBetter(String key, NodeLink newLink){
        return this.table.isLinkBetter(key, newLink);
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        
        sb.append("Vertex:\n");
        sb.append(this.node.toString());
        sb.append("\n");
        sb.append("Table:\n");
        sb.append(this.table.toString());

        return sb.toString();
    }
}
