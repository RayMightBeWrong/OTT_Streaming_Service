package overlay.state;

import java.util.ArrayList;
import java.util.List;

public class StreamLink {
    private List<String> nodes;
    private int id;
    private boolean active;
    private boolean withFails;
    private String failureAt;

    public StreamLink(String[] path, String rcv, int id, boolean withFails, String failureAt){
        this.id = id;
        this.active = true;
        this.withFails = withFails;
        this.failureAt = failureAt;
        this.nodes = new ArrayList<>();
        for(String node: path)
            this.nodes.add(node);
        this.nodes.add(rcv);
    }

    public StreamLink(String[] args, int id){
        this.id = id;
        this.nodes = new ArrayList<>();
        this.active = true;
        this.withFails = false;
        this.failureAt = "";
        for(String s: args)
            this.nodes.add(s);
    }

    public int getStreamID(){
        return this.id;
    }

    public List<String> getStream(){
        return this.nodes;
    }

    public boolean getActive(){
        return this.active;
    }
    
    public boolean getWithFails(){
        return this.withFails;
    }

    public void setStream(List<String> stream){
        this.nodes = stream;
    }

    public void setActive(boolean active){
        this.active = active;
    }

    public void setWithFails(boolean withFails){
        this.withFails = withFails;
    }

    public void addToFailure(String node){
        this.failureAt = node;
    }

    public String getServer(){
        return this.nodes.get(0);
    }

    public String getReceivingNode(){
        return this.nodes.get(this.nodes.size() - 1);
    }

    public String[] convertLinkToArgs(){
        String[] res = new String[nodes.size()];

        for(int i = 0; i < this.nodes.size(); i++)
            res[i] = this.nodes.get(i);

        return res;
    }

    public String findNextNode(String me, boolean order){
        String nextNode = "";

        for(int i = 0; i < this.nodes.size(); i++){
            if (me.equals(this.nodes.get(i))){
                if (order){
                    if (i - 1 >= 0)
                        nextNode = this.nodes.get(i - 1);
                }
                else{
                    if (i + 1 < this.nodes.size())
                        nextNode = this.nodes.get(i + 1);
                }
                break;
            }
        }

        if (nextNode.equals(""))
            return me;
        else
            return nextNode;
    }

    public boolean isNodeInStream(String node){
        boolean res = false;

        for(String key: this.nodes)
            if (key.equals(node)){
                res = true;
                break;
            }

        return res;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append("\t\tSTREAM ID: " + this.id + "\n");
        sb.append("\t\tRECEIVING STREAM: " + getReceivingNode() + "\n\t\tGOING THROUGH:");
        for(int i = 0; i < this.nodes.size() - 1; i++){
            sb.append(" " + this.nodes.get(i));
        }
        sb.append("\n");

        if (active)
            sb.append("\t\tACTIVE: TRUE\n");
        else
            sb.append("\t\tACTIVE: FALSE\n");

        if (withFails){
            sb.append("\t\tFAILURES OCURRED: TRUE\n");
            sb.append("\t\tFAILURE AT: " + this.failureAt + "\n");
        }
        else
            sb.append("\t\tFAILURES OCURRED: FALSE\n");

        return sb.toString();
    }
}
