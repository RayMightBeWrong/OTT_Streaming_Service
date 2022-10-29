package overlay;

public class NodeManager {
    public static void main(String[] args){
        if (args.length >= 1){

            if (args[0].equals("-server")){
                Thread server = new Thread(new TCPServer());
                server.start();

                try {
                    server.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            else if (args[0].equals("-client")){
                Thread client = new Thread(new TCPClient());
                client.start();

                try {
                    client.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
