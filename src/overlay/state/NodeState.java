package overlay.state;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class NodeState {
    private Vertex node;
    private DistancesTable table;
    private List<String> streams;
    private List<String> servers;
    private final ReentrantLock lock;
    
    public NodeState(Vertex node){
        this.node = node;
        this.table = new DistancesTable();
        this.streams = new ArrayList<>();
        this.servers = new ArrayList<>();
        this.lock = new ReentrantLock();
    }

    public DistancesTable getTable(){
        return this.table;
    }

    public List<String> getServers(){
        return this.servers;
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

    public void addStream(String dest){
        this.lock.lock();
        try{
            if (!this.streams.contains(dest));
                this.streams.add(dest);
        }
        finally{
            this.lock.unlock();
        }
    }

    public void removeStream(String dest){
        this.lock.lock();
        try{
            if (!this.streams.contains(dest));
                this.streams.remove(dest);
        }
        finally{
            this.lock.unlock();
        }
    }

    public void addServer(String server){
        this.lock.lock();
        try{
            boolean isPresent = false;
            
            for(String s: this.servers){
                if(s.equals(server)){
                    isPresent = true;
                    break;
                }
            }

            if (isPresent == false)
                this.servers.add(server);
        }
        finally{
            this.lock.unlock();
        }
    }

    public void removeServer(String server){
        this.lock.lock();
        try{
            if (!this.servers.contains(server));
                this.servers.remove(server);
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

    public boolean isLinkModified(String key, NodeLink newLink){
        return this.table.isLinkModified(key, newLink);
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        
        sb.append("Vertex:\n");
        sb.append(this.node.toString());
        sb.append("\n");
        sb.append("Table:\n");
        sb.append(this.table.toString());
        sb.append("\n");
        sb.append("Servers: ");
        for(String server: this.servers)
            sb.append(server + " ");
        sb.append("\n\n");
        sb.append("Streams:\n");
        for(String stream: this.streams)
            sb.append("\tRECEIVING STREAM: " + stream + "\n");

        return sb.toString();
    }
}
