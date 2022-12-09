package overlay.state;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
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

    public NodeLink getClosestFromList(List<String> list){
        boolean initial = true;
        NodeLink min = new NodeLink();

        for(String entry: list){
            if (this.table.containsKey(entry)){
                NodeLink tmp = this.table.get(entry);
                
                if (initial){
                    initial = false;
                    min = tmp;
                }
                else if (tmp.getCost() < min.getCost())
                    min = tmp;
            }
        }

        return min;
    }

    public boolean isLinkModified(String key, NodeLink newLink){
        if (!table.containsKey(key))
            return true;

        NodeLink oldLink = table.get(key);
        if (oldLink.getViaNode().equals(newLink.getViaNode())){
            Long diff = Math.abs(oldLink.getCost() - newLink.getCost());
            double diffPercentage = diff / (oldLink.getCost() * 1.0);

            if (diffPercentage > 0.2)
                return true;
        }
        else {
            if (oldLink.getCost() > newLink.getCost())
                return true;
        }

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
