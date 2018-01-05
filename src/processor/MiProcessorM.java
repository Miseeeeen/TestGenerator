package processor;

import java.util.HashSet;
import java.util.Stack;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.FuncDecl;
import com.microsoft.z3.Model;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;

import borMi.dataStructure.TestCase;
import borMi.parser.MiNode;
import borMi.parser.MiParser;
import borMi.parser.Scanner;

/**
 * 这个版本的MI将运算结果留在内存中
 * @author misen
 *
 */

public class MiProcessorM {
    public HashSet<TestCase> trueTestCases; // 使得expr为True的测试用例
    public HashSet<TestCase> falseTestCases; 
    public String dnf;

    public MiProcessorM(String dnf) {
        // MiProcessor can't detail with the boolean like a&b&c
        if (!dnf.contains("|")) {
            delegateBorProcessor();
            return;
        }
        
        MiNode root = buildAST(dnf);

        // build a default TestCase
        TestCase defaultTestCase = buildDefaultTestCase(root); 

        // gettrueTestCases
        buildTrueTestCases(root, defaultTestCase);

        // getfalseTestCases
        buildFalseTestCases(root, defaultTestCase);

        root.ctx.close();
    }
    
    private void delegateBorProcessor() {
        BorProcessor bor = new BorProcessor(dnf);
        this.trueTestCases = bor.trueTestCases;
        this.falseTestCases = bor.falseTestCases;
        return;
    }
    
    private MiNode buildAST(String dnf) {
        this.dnf = dnf + ";";
        Scanner sc = new Scanner(this.dnf);
        MiParser pr = new MiParser(sc);
        MiNode root = pr.getAST();
        return root;
    }

    /*
    private int countLeaves(MiNode root) {
        if (root.children.size() == 0) {
            return 1;
        }
        int count = 0;
        for (MiNode child : root.children) {
            count += countLeaves(child);
        }

        return count;
    }
    */
    
    private void buildTrueTestCases(MiNode root, TestCase defaultTestCase) {
        this.trueTestCases = new HashSet<TestCase>();
        for (MiNode i : root.children) {
            //System.out.println("computing true TestCases: " + counter + "/" + root.children.size());
            BoolExpr expr = i.expr;
            for (MiNode j : root.children) {
                if (i != j) {
                    expr = root.ctx.mkAnd(expr, root.ctx.mkNot(j.expr));
                }
            }
            this.trueTestCases.add(getTestCase(root.ctx, expr, defaultTestCase));
        }
        this.trueTestCases.remove(null);
    }

    private void buildFalseTestCases(MiNode root, TestCase defaultTestCase) {
        this.falseTestCases = new HashSet<TestCase>();
        for (MiNode i : root.children) {
            BoolExpr expr;

            // 没有孩子
            if (i.children.size() == 0) {
                expr = root.ctx.mkNot(i.expr);
                for (MiNode k : root.children) {
                    if (i != k) {
                        expr = root.ctx.mkAnd(expr, k.ctx.mkNot(k.expr));
                    }
                }
                this.falseTestCases.add(getTestCase(root.ctx, expr, defaultTestCase));
            }

            // 有孩子
            for (MiNode j : i.children) {
                expr = root.ctx.mkNot(j.expr);
                for (MiNode k : i.children) {
                    if (j != k) {
                        expr = root.ctx.mkAnd(expr, k.expr);
                    }
                }

                for (MiNode k : root.children) {
                    if (i != k) {
                        expr = root.ctx.mkAnd(expr, k.ctx.mkNot(k.expr));
                    }
                }

                this.falseTestCases.add(getTestCase(root.ctx, expr, defaultTestCase));
            }
        }
        this.falseTestCases.remove(null);
    }
    
    private TestCase buildDefaultTestCase(MiNode root) {
        TestCase defaultTestCase = new TestCase();
        Stack<MiNode> stack = new Stack<MiNode>();
        stack.push(root);
        while (!stack.isEmpty()) {
            MiNode tmp = stack.pop();
            if (tmp.children.size() == 0) {
                defaultTestCase.put(tmp.value, "true");
            }
            for (MiNode i : tmp.children) {
                stack.push(i);
            }
        }
        
        return defaultTestCase;
    }

    private TestCase getTestCase(Context ctx, BoolExpr expr, TestCase defaultTestCase) {
        Solver solver = ctx.mkSolver();
        solver.add(expr);

        TestCase testCase = new TestCase();
        for (String i : defaultTestCase.keySet()) {
            testCase.put(i, "true");
        }

        if (solver.check() == Status.SATISFIABLE) {
            Model model = solver.getModel();
            for (FuncDecl i : model.getConstDecls()) {
                testCase.put(i.getName().toString(), model.getConstInterp(i).toString());
            }
            return testCase;
        }
        else {
            //System.out.println("约束无解");
            return null;
        }
    }
}
