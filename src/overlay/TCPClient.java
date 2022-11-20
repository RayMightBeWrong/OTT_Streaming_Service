package overlay;

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
    public static final int SEND_ROUTES = 4;

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
            MessageSender sender = new MessageSender(out);

            switch(this.behaviour){
                case HELLO:
                    sender.hello(); break;

                case PROBE:
                    sender.probe(); break;

                case SEND_NEW_LINK:
                    NodeLink link = this.state.getLinkTo((String) extraInfo);
                    sender.sendNewLink(link, this.state.getSelf());
                    break;
                
                case SEND_ROUTES:
                    sender.sendRoutes(this.state, (String) extraInfo); break;
            }

            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        } 
    }
}
