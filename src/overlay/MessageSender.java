package overlay;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
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

    public void initialMessageBootstrapper(String nodeName, Map<String, InetAddress> adjs) throws IOException{
        sendSelfNodeInfo(nodeName);
        sendAdjacents(adjs);
        end();
    }

    public void sendSelfNodeInfo(String nodeName){
        String s = "YOU: " + nodeName;
        sendMessage(s);
    }

    public void sendAdjacents(Map<String, InetAddress> adjs){
        for(Map.Entry<String, InetAddress> entry: adjs.entrySet())
            out.println(entry.getKey() + ": " + entry.getValue().getHostAddress());
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

    public void nodeClosed(String nodeName){
        String s = "lost " + nodeName;
        sendMessage(s);
    }
}
