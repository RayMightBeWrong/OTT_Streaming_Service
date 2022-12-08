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
    public static final int REDIRECT = 2;
    public static final int PROBE_INITIAL = 3;
    public static final int PROBE_REGULAR = 4;
    public static final int SEND_NEW_LINK = 5;
    public static final int SEND_ROUTES = 6;
    public static final int INIT_MONITORING = 7;
    public static final int MONITORING = 8;
    public static final int OPEN_STREAM_CLIENT = 9;
    public static final int ASK_STREAMING = 10;
    public static final int NEW_STREAM = 11;
    public static final int OPEN_UDP_MIDDLEMAN = 12;
    public static final int ACK_OPEN_UDP_MIDDLEMAN = 13;


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

                case REDIRECT:
                    String msg = (String) extraInfo;
                    sender.sendMessage(msg);
                    break;

                case PROBE_INITIAL:
                    sender.probe(true); break;

                case PROBE_REGULAR:
                    sender.probe(false); break;

                case SEND_NEW_LINK:
                    NodeLink link = this.state.getLinkTo((String) extraInfo);
                    sender.sendNewLink(link, this.state.getSelf());
                    break;
                
                case SEND_ROUTES:
                    sender.sendRoutes(this.state, (String) extraInfo); break;

                case INIT_MONITORING:
                    sender.sendInitialMonitoringMessage(this.state); break;

                case MONITORING:
                    String[] nodesVisited = (String[]) extraInfo;
                    sender.sendMonitoringMessage(this.state, nodesVisited); break;

                case OPEN_STREAM_CLIENT:
                    sender.streamClient();
                    break;

                case ASK_STREAMING:
                    sender.sendAskStreaming(this.state); break;

                case NEW_STREAM:
                    String[] nodesInfo = (String[]) extraInfo;
                    String[] visited = new String[nodesInfo.length - 1];
                    for(int i = 0; i < nodesInfo.length - 1; i++){
                        visited[i] = nodesInfo[i];
                    }
                    String newDest = nodesInfo[nodesInfo.length - 1];
                    sender.sendNewStreamSignal(this.state, visited, newDest); 
                    break;

                case OPEN_UDP_MIDDLEMAN:
                    String dest = (String) extraInfo;
                    sender.sendOpenUDPMiddleManSignal(dest); break;

                case ACK_OPEN_UDP_MIDDLEMAN:
                    sender.ackOpenUDPMiddleManSignal(); break;       
            }

            socket.close();

        } catch (Exception e) {
            System.out.println("Connection refused with " + neighbor);
        }
    }
}
