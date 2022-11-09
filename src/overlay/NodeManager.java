package overlay;

public class NodeManager {
    public static void main(String[] args){
        if (args.length == 2){
            if (args[0].equals("-server")){
                ConfigParser parser = new ConfigParser(args[1]);
                Graph graph = parser.parseXML();

                Thread server = new Thread(new TCPServer(graph));
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
        else if (args.length < 2)
            System.out.println("Não foram inseridos argumentos suficientes!");
        else
            System.out.println("Foram inseridos demasiados argumentos!");
    }
}
