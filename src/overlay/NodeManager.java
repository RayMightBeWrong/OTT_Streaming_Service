package overlay;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class NodeManager {
    public static Graph getAllOverlayNodes(NodeList nodeList){
        Map<String, Vertex> graph = new HashMap<>();
        Element tmp = (Element) nodeList.item(0);

        NodeList entries = tmp.getElementsByTagName("entry");
        for(int i = 0; i < entries.getLength(); i++){
            Element entry = (Element) tmp.getElementsByTagName("entry").item(i);
            graph.put(entry.getAttribute("n"), null);
        }

        return new Graph(graph);
    }

    public static Vertex readNode(Element node){
        try{
            String name = node.getAttribute("n");
            String ipName = node.getElementsByTagName("ip").item(0).getTextContent();
            InetAddress ip = InetAddress.getByName(ipName);

            List<String> adjs = new ArrayList<>();
            NodeList adjacents = node.getElementsByTagName("adj");
            for(int i = 0; i < adjacents.getLength(); i++)
                adjs.add(adjacents.item(i).getTextContent());

            return new Vertex(name, ip, adjs);
        }
        catch (Exception e){
            return null;
        }
    }

    public static void readOverlayNodes(NodeList nodes, Graph g){
        for(int i = 0; i < nodes.getLength(); i++){
            Element node = (Element) nodes.item(i);
            Vertex v = readNode(node);
            g.addNode(v);
        }
    }

    public static void parseXML(File file){
        try{
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("nodes");
            NodeList nodes = doc.getElementsByTagName("node");

            Graph g = getAllOverlayNodes(nodeList);
            readOverlayNodes(nodes, g);

            System.out.println(g);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        if (args.length == 2){
            if (args[0].equals("-server")){
                File f = new File(args[1]);
                parseXML(f);

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
        else if (args.length < 2)
            System.out.println("Não foram inseridos argumentos suficientes!");
        else
            System.out.println("Foram inseridos demasiados argumentos!");
    }
}
