package overlay;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class DistancesTable {
    private Map<String, NodeLink> table;

    public DistancesTable(){
        this.table = new HashMap<>();
    }

    public void addLink(String dest, String viaNode, InetAddress viaInterface, long cost){
        NodeLink link = new NodeLink(dest, viaNode, viaInterface, cost);
        this.table.put(dest, link);
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();

        for(Map.Entry<String, NodeLink> entry: this.table.entrySet()){
            sb.append(entry.getValue().toString());
        }

        return sb.toString();
    }
}
