package overlay;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class DistancesTable {
    private Map<String, NodeLink> table;

    public DistancesTable(){
        this.table = new HashMap<>();
    }

    public Map<String, NodeLink> getTable(){
        return this.table;
    }

    public void addLink(String dest, NodeLink newLink){
        this.table.put(dest, newLink);
    }

    public void addLink(String dest, String viaNode, InetAddress viaInterface, long cost){
        NodeLink link = new NodeLink(dest, viaNode, viaInterface, cost);
        this.table.put(dest, link);
    }

    public NodeLink getLinkTo(String key){
        return this.table.get(key);
    }

    public boolean isLinkBetter(String key, NodeLink nodeLink){
        if (!table.containsKey(key))
            return true;
        else
            return false;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();

        for(Map.Entry<String, NodeLink> entry: this.table.entrySet()){
            sb.append(entry.getValue().toString());
        }

        return sb.toString();
    }
}
