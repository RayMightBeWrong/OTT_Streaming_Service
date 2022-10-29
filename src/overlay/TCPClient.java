package overlay;

//import java.io.BufferedReader;
//import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class TCPClient extends Thread{
    public void run(){
        try {
            Socket socket = new Socket("127.0.0.1", 6666);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            //BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out.println("bruh");
            out.flush();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
