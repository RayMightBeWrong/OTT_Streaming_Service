package overlay;

public class NodeState {
    private Vertex node;
    private DistancesTable table;
    
    public NodeState(Vertex node){
        this.node = node;
        this.table = new DistancesTable();
    }
}
