package activityGraph;

public class Edge {
    public String id;
    public String guardExpr;

    public Node target;
    public Node source;

    public String targetId;
    public String sourceId;

    public Edge(String id, String guardExpr, String targetId, String sourceId) {
        this.id = id;
        this.guardExpr = guardExpr;
        this.targetId = targetId;
        this.sourceId = sourceId;
    }

}
