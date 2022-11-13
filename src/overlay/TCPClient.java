package overlay;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class TCPClient extends Thread{
    private NodeState state;
    private InetAddress neighbor;
    private int behaviour;
    
    private Object extraInfo;

    public static final int HELLO = 1;
    public static final int PROBE = 2;
    public static final int SEND_NEW_LINK = 3;
    public static final int SEND_TOPOLOGY = 4;

    public TCPClient(NodeState state, InetAddress neighbor, int behaviour){
        this.state = state;
        this.neighbor = neighbor;
        this.behaviour = behaviour;
    }

    public TCPClient(NodeState state, InetAddress neighbor, int behaviour, Object extraInfo){
        this.state = state;
        this.neighbor = neighbor;
        this.behaviour = behaviour;
        this.extraInfo = extraInfo;
    }

    public void run(){
        try {
            Socket socket = new Socket(this.neighbor, TCPServer.PORT);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            MessageSender sender = new MessageSender(out);

            switch(this.behaviour){
                case HELLO:
                    helloBehaviour(sender, in); break;

                case PROBE:
                    probeBehaviour(sender); break;

                case SEND_NEW_LINK:
                    sendNewLinkBehaviour(sender); break;
                
                case SEND_TOPOLOGY:
                    sendTopologyBehaviour(sender); break;
            }

            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        } 
    }

    public void helloBehaviour(MessageSender sender, BufferedReader in) throws IOException{
        sender.hello();
    }

    public void probeBehaviour(MessageSender sender){
        sender.probe();
    }

    public void sendNewLinkBehaviour(MessageSender sender){
        NodeLink link = this.state.getLinkTo((String) extraInfo);
        sender.sendNewLink(link);
    }

    public void sendTopologyBehaviour(MessageSender sender){
        sender.sendMessage("bruh");
    }
}
