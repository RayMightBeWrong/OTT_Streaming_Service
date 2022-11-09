package overlay;

import java.io.File;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ConfigParser {
    private File file;
    public static String bstrapperName = "O1";
 
    public ConfigParser(String filepath){
        this.file = new File(filepath);
    }

    public Graph parseXML(){
        try{
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);
            doc.getDocumentElement().normalize();

            NodeList nodeList = doc.getElementsByTagName("nodes");
            NodeList nodes = doc.getElementsByTagName("node");

            Graph g = getAllOverlayNodes(nodeList);
            readOverlayNodes(nodes, g);
            return g;
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public Graph getAllOverlayNodes(NodeList nodeList){
        Map<String, Vertex> graph = new HashMap<>();
        Element tmp = (Element) nodeList.item(0);

        NodeList entries = tmp.getElementsByTagName("entry");
        for(int i = 0; i < entries.getLength(); i++){
            Element entry = (Element) tmp.getElementsByTagName("entry").item(i);
            graph.put(entry.getAttribute("n"), null);
        }

        return new Graph(graph, bstrapperName);
    }

    public Vertex readNode(Element node){
        try{
            String name = node.getAttribute("n");
            String ipName = node.getElementsByTagName("ip").item(0).getTextContent();
            InetAddress ip = InetAddress.getByName(ipName);

            Map<String, InetAddress> adjs = new HashMap<>();
            NodeList adjacents = node.getElementsByTagName("adj");
            for(int i = 0; i < adjacents.getLength(); i++){
                adjs.put(adjacents.item(i).getTextContent(), null);
            }

            return new Vertex(name, ip, adjs);
        }
        catch (Exception e){
            return null;
        }
    }

    public void readOverlayNodes(NodeList nodes, Graph g){
        for(int i = 0; i < nodes.getLength(); i++){
            Element node = (Element) nodes.item(i);
            Vertex v = readNode(node);
            g.addNode(v);
        }
    }
}
