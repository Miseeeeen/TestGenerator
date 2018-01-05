package activityGraph;

import java.util.List;

public class PathConstraint {
    public String inputExpr;
    public String outputExpr;
    
    public PathConstraint(String inputExpr, String outputExpr) {
        this.inputExpr = inputExpr;
        this.outputExpr = outputExpr;
    }

    public PathConstraint clone() {
        return new PathConstraint(inputExpr, outputExpr);
    }
}
