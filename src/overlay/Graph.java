package overlay;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Graph {
    private Map<String, Vertex> nodes;

    public Graph(Map<String, Vertex> nodes){
        this.nodes = nodes;
    }

    public void addNode(Vertex v){
        if (nodes.containsKey(v.getName()))
            nodes.put(v.getName(), v);
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, Vertex> node: this.nodes.entrySet()){
            if (node.getValue() == null)
                sb.append("Node: " + node.getKey() + "\n");
            else
                sb.append(node.getValue().toString() + "\n");
        }

        return sb.toString();
    }

    public String getNameFromIP(InetAddress ip){
        String ipString = ip.getHostAddress();
        String nodeName = "";

        for (Map.Entry<String, Vertex> entry: this.nodes.entrySet()){
            if (ipString.equals(entry.getValue().getIp().getHostAddress()))
                nodeName = entry.getKey();
        }

        if (nodeName.equals(""))
            return null;
        else
            return nodeName;
    }

    public Map<String, InetAddress> getNodeAdjacents(String key){
        Vertex node = this.nodes.get(key);        
        Map<String, InetAddress> adjs = node.getAdjacents();

        Map<String, InetAddress> res = new HashMap<>();
        for (Map.Entry<String, InetAddress> adj: adjs.entrySet()){
            Vertex tmp = this.nodes.get(adj.getKey());
            res.put(tmp.getName(), tmp.getIp());
        }

        return res;
    }

    public List<String> deleteNode(String name){
        Vertex node = this.nodes.get(name);
        List<String> adjs = new ArrayList<>(node.getAdjacents().keySet());

        for (String adj: adjs){
            Vertex tmp = this.nodes.get(adj);
            tmp.removeAdjacent(name);
        }

        this.nodes.remove(name);
        return adjs;
    }
}
