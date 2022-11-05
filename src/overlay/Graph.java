package overlay;

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
}
