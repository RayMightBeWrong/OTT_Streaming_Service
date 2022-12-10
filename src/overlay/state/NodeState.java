package overlay.state;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;


public class NodeState {
    private Vertex node;
    private InetAddress bstrapperIP;
    private DistancesTable table;
    private List<StreamLink> streams;
    private List<String> servers;
    private final ReentrantLock lock;
    
    public NodeState(Vertex node, InetAddress bstrapperIP){
        this.node = node;
        this.bstrapperIP = bstrapperIP;
        this.table = new DistancesTable();
        this.streams = new ArrayList<>();
        this.servers = new ArrayList<>();
        this.lock = new ReentrantLock();
    }

    public InetAddress getBstrapperIP(){
        return this.bstrapperIP;
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

    public List<InetAddress> getSelfIPs(){
        return this.node.getIPList();
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

    public void addStream(StreamLink stream){
        this.lock.lock();
        try{
            if (!this.streams.contains(stream));
                this.streams.add(stream);
        }
        finally{
            this.lock.unlock();
        }
    }

    public void removeStream(StreamLink stream){
        this.lock.lock();
        try{
            if (!this.streams.contains(stream));
                this.streams.remove(stream);
        }
        finally{
            this.lock.unlock();
        }
    }

    public void addServer(String server){
        this.lock.lock();
        try{
            if(!server.equals("")){
                boolean isPresent = isServer(server);

                if (isPresent == false)
                    this.servers.add(server);
            }
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

    public boolean isServer(String server){
        boolean isPresent = false;
            
        for(String s: this.servers){
            if(s.equals(server)){
                isPresent = true;
                break;
            }
        }

        return isPresent;
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

    public NodeLink getClosestServer(){
        return this.table.getClosestFromList(this.servers);
    }

    public boolean isLinkModified(String key, NodeLink newLink){
        return this.table.isLinkModified(key, newLink);
    }

    public StreamLink getMyStream(){
        StreamLink res = null;

        for(StreamLink stream: this.streams){
            if (stream.getReceivingNode().equals(getSelf())){
                res = stream;
                break;
            }
        }

        return res;
    }

    public StreamLink getStreamFromArgs(String[] args){
        StreamLink res = null;

        for(StreamLink stream: this.streams){
            if (args[0].equals(stream.getServer()) && args[args.length - 1].equals(stream.getReceivingNode())){
                res = stream;
                break;
            }
        }

        return res;
    }

    public List<String> handleClosedNode(String key){
        setAdjState(key, Vertex.OFF);
        if (isServer(key))
            removeServer(key);

        return this.table.handleClosedNode(key);
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
        for(StreamLink stream: this.streams)
            sb.append(stream.toString() + "\n");

        return sb.toString();
    }
}
