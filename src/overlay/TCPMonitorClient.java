package overlay;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

public class TCPMonitorClient extends TimerTask{
    private NodeState state;

    public TCPMonitorClient(NodeState state){
        this.state = state;
    }

    @Override
    public void run(){
        Map<String, Integer> adjsState = this.state.getNodeAdjacentsState();
        Map<String, List<InetAddress>> adjs = this.state.getNodeAdjacents();

        for(Map.Entry<String, Integer> entry: adjsState.entrySet()){
            if (entry.getValue() == Vertex.ON){
                List<InetAddress> ips = adjs.get(entry.getKey());
                Thread client = new Thread(new TCPClient(this.state, ips.get(0), TCPClient.INIT_MONITORING));
                client.start();
                
                try {
                    client.join();
                } catch (InterruptedException e) {
                    // TODO - turn off adjacente
                    //this.state.turnOffAdj();
                }
            }
        }
    }
}
