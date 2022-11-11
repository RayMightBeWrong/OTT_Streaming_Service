package overlay;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;

public class MessageSender {
    private PrintWriter out;

    public MessageSender(PrintWriter out){
        this.out = out;
    }

    public void sendMessage(String msg){
        out.println(msg);
        out.flush();
    }


    /*  BOOTSTRAPPER MESSAGES */

    public void initialMessageBootstrapper(String nodeName, Map<String, List<InetAddress>> adjs, Map<String, Integer> adjsState) throws IOException{
        sendSelfNodeInfo(nodeName);
        sendAdjacents(adjs, adjsState);
        end();
    }

    public void sendSelfNodeInfo(String nodeName){
        String s = "YOU: " + nodeName;
        sendMessage(s);
    }

    public void sendAdjacents(Map<String, List<InetAddress>> adjs, Map<String, Integer> adjsState){
        for(Map.Entry<String, List<InetAddress>> entry: adjs.entrySet()){
            Integer state = adjsState.get(entry.getKey());

            if (state == Vertex.OFF)
                out.println("ADJ: " + entry.getKey() + ": OFF");
                
            else if (state == Vertex.ON)
                out.println(entry.getKey() + ": ON");

            for(InetAddress ip: entry.getValue())
                out.println("Available at: " + ip.getHostAddress());
        }

        out.flush();
    }


    
    /*  CLIENT MESSAGES */

    public void initialMessageClient(){
        sendMessage("hello");
    }



    /*  MESSAGES SENT BY ALL*/

    public void end(){
        sendMessage("end");
    }

    public void ping(){
        sendMessage("ping");
    }

    public void pingAck(){
        sendMessage("pingAck");
    }
}
