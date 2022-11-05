package overlay;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
//import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


public class TCPServer extends Thread{
    public void run(){
        try{
            ServerSocket server = new ServerSocket(6666);
            
            while(true){
                Socket client = server.accept();
                //PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                System.out.println(in.readLine());
                System.out.println("c'mon");
            }

            //server.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
