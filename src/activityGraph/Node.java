package activityGraph;

import java.util.ArrayList;
import java.util.List;

public class Node {
    public String type;
    public String id;
    public String name;
    public List<Edge> outgoings; 
    public List<String> outgoingIds;
    
    
    public Node(String type, String id, String name, List<String> outgoingIds) {
        this.type = type;
        this.id = id;
        this.name = name;
        this.outgoingIds = outgoingIds;
        this.outgoings = new ArrayList<Edge>();
    }

    public boolean isInitialNode() {
        return type.equals("InitialNode");
    }

    public boolean isFinalNode() {
        return type.equals("ActivityFinalNode");
    }
    
    public boolean isOutputNode() {
        return type.equals("CreateLinkAction");
    }
    
}
