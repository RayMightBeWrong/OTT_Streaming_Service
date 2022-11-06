package overlay;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
//import java.io.BufferedReader;
//import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class TCPClient extends Thread{
    private Vertex node;

    public void run(){
        try {
            Socket socket = new Socket("127.0.0.1", 6666);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println("hello");
            out.flush();

            this.node = readInitialMsg(in, socket);
            introduceToAdjs();


            socket.close();

            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Vertex readInitialMsg(BufferedReader in, Socket socket) throws IOException{
        String name = "";
        Map<String, InetAddress> adjs = new HashMap<>();

        while(true){
            String s = in.readLine();
            if(s.equals("close"))
                break;

            String[] tokens = s.split(": ");
            if (tokens[0].equals("YOU"))
                name = tokens[1];
            else
                adjs.put(tokens[0], java.net.InetAddress.getByName(tokens[1]));
        }

        return new Vertex(name, socket.getLocalAddress(), adjs);
    }

    public void introduceToAdjs(){
        for (Map.Entry<String, InetAddress> adj: this.node.getAdjacents().entrySet()){
            
        }
    }
}
