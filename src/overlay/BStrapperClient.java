package overlay;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BStrapperClient{

    public static NodeState readInitialMsg(String bstrapper){
        try {
            Socket socket = new Socket(InetAddress.getByName(bstrapper), BStrapper.PORT);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            MessageSender sender = new MessageSender(out);

            sender.hello();
            NodeState state = getInitialMsg(in, socket);
            socket.close();

            return state;
        } 
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static NodeState getInitialMsg(BufferedReader in, Socket socket) throws IOException{
        String name = "";
        Map<String, List<InetAddress>> adjs = new HashMap<>();
        Map<String, Integer> adjsState = new HashMap<>();

        String currentAdj = "";
        while(true){
            String msg = in.readLine();
            if(msg.equals("end"))
                break;

            String[] tokens = msg.split(": ");
            if (tokens[0].equals("YOU"))
                name = tokens[1];

            if (tokens[0].equals("ADJ")){
                currentAdj = tokens[1];
                
                if (tokens[2].equals("OFF"))
                    adjsState.put(currentAdj, Vertex.OFF);
                else if (tokens[2].equals("ON"))
                    adjsState.put(currentAdj, Vertex.ON);

                adjs.put(currentAdj, new ArrayList<>());
            }

            else if (tokens[0].equals("Available at")){
                List<InetAddress> list = adjs.get(currentAdj);
                list.add((InetAddress) InetAddress.getByName(tokens[1]));
                adjs.put(currentAdj, list);
            }
        }

        Vertex v = new Vertex(name, adjs, adjsState, Vertex.ON);
        return new NodeState(v);
    }
}
