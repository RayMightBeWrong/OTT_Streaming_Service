package overlay.state;

import java.util.ArrayList;
import java.util.List;

public class StreamLink {
    private List<String> nodes;
    private int id;

    public StreamLink(String[] args, int id){
        this.id = id;
        this.nodes = new ArrayList<>();
        for(String s: args)
            this.nodes.add(s);
    }

    public int getStreamID(){
        return this.id;
    }

    public List<String> getStream(){
        return this.nodes;
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

    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append("\t\tSTREAM ID: " + this.id + "\n");
        sb.append("\t\tRECEIVING STREAM: " + getReceivingNode() + "\n\t\tGOING THROUGH:");
        for(int i = 0; i < this.nodes.size() - 1; i++){
            sb.append(" " + this.nodes.get(i));
        }
        sb.append("\n");

        return sb.toString();
    }
}
