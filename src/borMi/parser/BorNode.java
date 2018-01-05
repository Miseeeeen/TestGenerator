package borMi.parser;

import java.util.ArrayList;
import java.util.HashSet;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;

import borMi.dataStructure.TestCase;
import borMi.visitor.Visitor;

public class BorNode {
    public String value;
    public HashSet<TestCase> trueTestCases = new HashSet<TestCase>();
    public HashSet<TestCase> falseTestCases = new HashSet<TestCase>();
    public ArrayList<BorNode> children = new ArrayList<BorNode>();

    public static Context ctx = new Context();
    public BoolExpr boolExpr;

    public BorNode() {

    }

    public BorNode(String value) {
        this.value = value;
        trueTestCases.add(new TestCase() {
            {
                put(value, "true");
            }
        });
        falseTestCases.add(new TestCase() {
            {
                put(value, "false");
            }
        });
    }

    public BorNode(BorNode child, String value) {
        this.value = value;
        this.children.add(child);
    }

    public BorNode(BorNode left, BorNode right, String value) {
        this.value = value;
        this.children.add(left);
        this.children.add(right);
    }

    public int getChildrenSize() {
        return children.size();
    }

    public BorNode getFirstChild() {
        if (children.size() < 1) {
            return null;
        }
        return children.get(0);
    }

    public BorNode getSecondChild() {
        if (children.size() < 2) {
            return null;
        }
        return children.get(1);
    }

    public String getValue() {
        return value;
    }

    public HashSet<TestCase> getTrueTestCases() {
        return trueTestCases;
    }

    public HashSet<TestCase> getFalseTestCases() {
        return falseTestCases;
    }

    public void setTrueTestCases(HashSet<TestCase> trueTestCases) {
        this.trueTestCases = trueTestCases;
    }

    public void setFalseTestCases(HashSet<TestCase> falseTestCases) {
        this.falseTestCases = falseTestCases;
    }

    public void accept(Visitor visitor) { // visitor模式, 委托visitor去访问自己
        visitor.visit(this);
    }

    public void buildBoolExpr() {
        // 对应mutiMiProcessor中的重建过程做此处理
        if (children.size() == 0) {
            if (boolExpr == null) {
                this.boolExpr = ctx.mkBoolConst(value);
            }
        }
        else if (children.size() == 1) {
            children.get(0).buildBoolExpr();
            this.boolExpr = ctx.mkNot(children.get(0).boolExpr);
        }
        else if (value.equals("&")) {
            children.get(0).buildBoolExpr();
            children.get(1).buildBoolExpr();
            this.boolExpr = ctx.mkAnd(children.get(0).boolExpr, children.get(1).boolExpr);
        }
        else if (value.equals("|")) {
            children.get(0).buildBoolExpr();
            children.get(1).buildBoolExpr();
            this.boolExpr = ctx.mkOr(children.get(0).boolExpr, children.get(1).boolExpr);
        }
    }
    
}
