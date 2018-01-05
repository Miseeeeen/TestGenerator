package activityGraph;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GraphParser {
    UmlFile file;

    public Graph parse(String path) {
        file = new UmlFile(path);
        return activity(file);
    }

    private Graph activity(UmlFile file) {
        file.advance(); // <?xml version
        file.advance(); // <uml:Model xmi:versio
        file.advance(); // <packagedElement

        Graph graph = new Graph();
        graph.edges = edges(file);
        graph.nodes = nodes(file);

        link(graph.edges, graph.nodes);

        file.advance(); // </packagedElement>
        file.advance(); // </uml:Model>
        return graph;
    }

    private List<Edge> edges(UmlFile file) {
        List<Edge> edges = new ArrayList<Edge>();

        while (isEdge(file)) {
            edges.add(edge(file));
        }

        return edges;
    }

    private Edge extractEdge(String regex, String line) {
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(line);
        m.find();

        String id = m.group(2);
        String name = m.group(3);
        String target = m.group(4);
        String source = m.group(5);

        // 暂时用name来表示guardExpr
        return new Edge(id, name, target, source);
    }

    private Edge edge(UmlFile file) {
        String line = file.currentLine().trim();
        file.advance();
        String regex = "<edge\\s*xmi:type=\"uml:([^\"]*)\"\\s*xmi:id=\"([^\"]*)\"\\s*name=\"([^\"]*)\"\\s*target=\"([^\"]*)\"\\s*source=\"([^\"]*)\"/>";

        return extractEdge(regex, line);
    }

    private List<Node> nodes(UmlFile file) {
        List<Node> nodes = new ArrayList<Node>();

        while (isNode(file)) {
            nodes.add(node(file));
        }

        return nodes;
    }
    
    /**
     * 根据正则表达式从String中提取出Node
     * @param regex
     * @param line
     * @return
     */

    private Node extractNode(String regex, String line) {
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(line);
        m.find();

        String type = m.group(1);
        String id = m.group(2);
        String name = m.group(3);
        String outgoing = m.groupCount() > 3 ? m.group(4) : null;

        List<String> outgoingIds =
                outgoing == null ? new ArrayList<String>() : Arrays.asList(outgoing.split(" "));

        return new Node(type, id, name, outgoingIds);
    }

    private Node node(UmlFile file) {
        String line = file.currentLine().trim();
        file.advance();

        if (line.contains("InitialNode")) {
            String regex =
                    "<node\\s*xmi:type=\"uml:([^\"]*)\"\\s*xmi:id=\"([^\"]*)\"\\s*name=\"([^\"]*)\"\\s*outgoing=\"([^\"]*)\"/>";
            return extractNode(regex, line);
        }
        else if (line.contains("ActivityFinalNode")) {
            String regex =
                    "<node\\s*xmi:type=\"uml:([^\"]*)\"\\s*xmi:id=\"([^\"]*)\"\\s*name=\"([^\"]*)\"\\s*incoming=\"[^\"]*\"/>";
            return extractNode(regex, line);
        }
        else {
            String regex =
                    "<node\\s*xmi:type=\"uml:([^\"]*)\"\\s*xmi:id=\"([^\"]*)\"\\s*name=\"([^\"]*)\"\\s*incoming=\"[^\"]*\"\\s*outgoing=\"([^\"]*)\"/>";
            return extractNode(regex, line);
        }

    }
    
    /**
     * 把node和edge连接起来
     * @param edges
     * @param nodes
     */

    private void link(List<Edge> edges, List<Node> nodes) {
        HashMap<String, Edge> edgeMap = new HashMap<String, Edge>();
        HashMap<String, Node> nodeMap = new HashMap<String, Node>();

        for (Edge edge : edges) {
            edgeMap.put(edge.id, edge);
        }

        for (Node node : nodes) {
            nodeMap.put(node.id, node);
        }

        for (Edge edge : edges) {
            edge.source = nodeMap.get(edge.sourceId);
            edge.target = nodeMap.get(edge.targetId);
        }

        for (Node node : nodes) {
            if (!node.isFinalNode()) {
                for (String outgoingId : node.outgoingIds) {
                    node.outgoings.add(edgeMap.get(outgoingId));
                }
            }
        }
    }

    private boolean isEdge(UmlFile file) {
        return file.currentLine().contains("edge");
    }

    private boolean isNode(UmlFile file) {
        return file.currentLine().contains("node");
    }

}
