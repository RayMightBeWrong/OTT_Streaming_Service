package overlay;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class BStrapperClient extends Thread{
    private String bstrapper;

    public BStrapperClient(String bstrapper){
        this.bstrapper = bstrapper;
    }

    public static Graph readInitialMsg(String bstrapper){
        try {
            Socket socket = new Socket(InetAddress.getByName(bstrapper), BStrapper.PORT);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            MessageSender sender = new MessageSender(out);

            sender.initialMessageClient();
            Graph graph = getInitialMsg(in, socket);
            socket.close();

            return graph;
        } 
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Graph getInitialMsg(BufferedReader in, Socket socket) throws IOException{
        String name = "";
        Map<String, InetAddress> adjs = new HashMap<>();

        while(true){
            String s = in.readLine();
            if(s.equals("end"))
                break;

            String[] tokens = s.split(": ");
            if (tokens[0].equals("YOU"))
                name = tokens[1];
            else
                adjs.put(tokens[0], java.net.InetAddress.getByName(tokens[1]));
        }

        //Graph graph = new Graph(name);
        //graph.buildGraphFromAdjs(adjs);
        //System.out.println(graph);
        //return graph;
        return null;
    }

    public void run(){

    }
}
