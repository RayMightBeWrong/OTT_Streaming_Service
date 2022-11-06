package overlay;

public class ClientManager extends Thread{
    private Vertex node;
    private String adjNode;
    private InetAddress adjIP;

    public ClientManager(Vertex node){
        this.node = node;
    }

    public void run(){

    }
}
