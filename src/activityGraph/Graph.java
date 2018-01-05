package activityGraph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Graph {
    List<Node> nodes;
    List<Edge> edges;
    
    public Node getInitialNode() {
        for(Node node: nodes){
            if(node.isInitialNode()){
                return node;
            }
        }
        
        return null;
    }

}
