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

    public int getNrStreams(){
        return this.streams.size();
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
            if (!this.streams.contains(stream))
                this.streams.add(stream);
        }
        finally{
            this.lock.unlock();
        }
    }

    public void removeStream(StreamLink stream){
        this.lock.lock();
        try{
            for(int i = 0; i < this.streams.size(); i++){
                StreamLink tmp = this.streams.get(i);
                if (tmp != null){
                    if (tmp.getReceivingNode().equals(stream.getReceivingNode()) && tmp.getServer().equals(stream.getServer())){
                        this.streams.remove(i);
                        this.streams.add(i, null);
                    }
                }
            }
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
            if (!this.servers.contains(server))
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
        return this.table.isLinkModified(getSelf(), key, newLink);
    }

    public StreamLink getMyStream(){
        StreamLink res = null;

        for(StreamLink stream: this.streams){
            if (stream != null){
                if (stream.getReceivingNode().equals(getSelf())){
                    res = stream;
                    break;
                }
            }
        }

        return res;
    }

    public StreamLink getStreamFromID(int ID){
        StreamLink res = null;

        for(StreamLink stream: this.streams){
            if (stream != null){
                if (stream.getStreamID() == ID){
                    res = stream;
                    break;
                }
            }
        }

        return res;
    }

    public StreamLink getStreamFromArgs(String[] args){
        StreamLink res = null;

        for(StreamLink stream: this.streams){
            if (stream != null){
                if (args[0].equals(stream.getServer()) && args[args.length - 1].equals(stream.getReceivingNode())){
                    res = stream;
                    break;
                }
            }
        }

        return res;
    }

    public boolean anyActiveStream(){
        boolean res = false;

        for (StreamLink stream: this.streams){
            if (stream != null){
                res = true;
                break;
            }
        }

        return res;
    }

    public boolean isNodeInStream(String node){
        boolean res = false;

        for (StreamLink stream: this.streams){
            if (stream != null)
                res = stream.isNodeInStream(node);
            if (res == true)
                break;
        }

        return res;
    }

    public List<StreamLink> getStreamsWithNode(String node){
        List<StreamLink> res = new ArrayList<>();

        for (StreamLink stream: this.streams){
            if (stream != null){
                if (stream.isNodeInStream(node)){
                    res.add(stream);
                }
            }
        }

        return res;
    }

    public boolean isNodeReceivingStream(String node){
        boolean res = false;

        for (StreamLink stream: this.streams){
            if (stream != null){
                if(node.equals(stream.getReceivingNode())){
                    res = true;
                    break;
                }
            }
        }

        return res;
    }

    public StreamLink getStreamFromReceivingNode(String node){
        StreamLink res = null;

        for (StreamLink stream: this.streams){
            if (stream != null){
                if(node.equals(stream.getReceivingNode())){
                    res = stream;
                    break;
                }
            }
        }

        return res;
    }

    public List<String> handleClosedNode(String key){
        setAdjState(key, Vertex.OFF);
        if (isServer(key))
            removeServer(key);

        if (isNodeInStream(key)){
            for(StreamLink stream: this.streams){
                if (stream != null){
                    if (stream.isNodeInStream(key)){
                        stream.setActive(false);
                        stream.setWithFails(true);
                        stream.addToFailure(key);
                    }
                }
            }
        }

        return this.table.handleClosedNode(key);
    }

    public StreamLink fixStream(String streamIDs, String rcvNode, String[] nodesVisited){
        StreamLink res = null;
        int streamID = Integer.parseInt(streamIDs);

        for(StreamLink stream: this.streams){
            if (stream != null){
                if (stream.getStreamID() == streamID){
                    stream.setActive(true);

                    List<String> old = stream.getStream();
                    List<String> newPath = new ArrayList<>();

                    for(String oldNode: old){
                        newPath.add(oldNode);
                        if (oldNode.equals(nodesVisited[0]))
                            break;
                    }

                    for (int i = 1; i < nodesVisited.length; i++)
                        newPath.add(nodesVisited[i]);
                    newPath.add(rcvNode);
                    
                    stream.setStream(newPath);
                    res = stream;
                }
            }
        }

        return res;
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
        for(int i = 0; i < this.streams.size(); i++){
            sb.append("\tSTREAM " + (i+1) + ":\n");
            StreamLink stream = this.streams.get(i);
            if (stream != null)
                sb.append(stream.toString() + "\n");
            else
                sb.append("\t\tit's done\n");
        }

        return sb.toString();
    }
}
