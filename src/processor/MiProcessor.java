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

import java.io.FileWriter;
import java.io.IOException;

/**
 * 这个版本的MiProcessor会直接将结果输出到文件中, 不会留在内存中
 * @author misen
 *
 */

public class MiProcessor {
    public HashSet<TestCase> trueTestCases;
    public HashSet<TestCase> falseTestCases;
    public String dnf;
    private int counter = 0;

    // MI模块对a&b&c这样的式子不能处理
    public MiProcessor(String dnf) throws IOException {

        if (!dnf.contains("|")) {
            /*
             * to-do, delegateBorProcessor
             */
            return;
        }

        MiNode root = buildAST(dnf);

        // build a default constraint
        TestCase defaultTestCase = buildDefaultTestCase(root);

        // getTrueTestCases
        buildTrueTestCases(root, defaultTestCase);

        // getFalseTestCases
        buildFalseTestCases(root, defaultTestCase);

        root.ctx.close();
    }

    private void buildTrueTestCases(MiNode root, TestCase defaultTestCase) throws IOException {
        this.trueTestCases = new HashSet<TestCase>();
        for (MiNode i : root.children) {
            System.out.println("computing");
            BoolExpr expr = i.expr;
            for (MiNode j : root.children) {
                if (i != j) {
                    expr = root.ctx.mkAnd(expr, root.ctx.mkNot(j.expr));
                }
            }
            this.trueTestCases.add(getTestCase(root.ctx, expr, defaultTestCase));

        }

        this.trueTestCases.remove(null);
        if (trueTestCases != null) {
            appendToFile("./true_constraint", trueTestCases);
        }
    }

    private void buildFalseTestCases(MiNode root, TestCase defaultTestCase) throws IOException {
        this.falseTestCases = new HashSet<TestCase>();

        // int leaves = countLeaves(root);
        // long start;
        // long end;

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

                // start = System.currentTimeMillis();

                this.falseTestCases.add(getTestCase(root.ctx, expr, defaultTestCase));

                // System.out.println((counter) + "/" + leaves);
                // end = System.currentTimeMillis();
                // System.out.println("预计时间: " + (end - start) * (leaves - counter) / 60000 + "
                // mins");
                reFresh(root);
            }
        }

        this.falseTestCases.remove(null);

        if (falseTestCases != null) {
            appendToFile("./false_constraint", falseTestCases);
        }
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

    private MiNode buildAST(String dnf) {
        this.dnf = dnf + ";";
        Scanner sc = new Scanner(this.dnf);
        MiParser pr = new MiParser(sc);
        MiNode root = pr.getAST();
        return root;
    }

    // 边运算 边将结果写入文件 乱 待改
    private void appendToFile(String path, HashSet<TestCase> testCases) throws IOException {
        for (TestCase c : testCases) {
            write(path, c.toString() + "\r\n");
        }
    }

    private void write(String path, String testCase) throws IOException {
        try {
            FileWriter writer = new FileWriter(path, true);
            writer.write(testCase);
            writer.close();
        } catch (Exception e) {

        }
    }

    private TestCase getTestCase(Context ctx, BoolExpr expr, TestCase defaultTestCase) {
        Solver solver = ctx.mkSolver();
        solver.add(expr);

        TestCase testCase = new TestCase();
        for (String name : defaultTestCase.variableSet()) {
            testCase.put(name, "true");
        }

        if (solver.check() == Status.SATISFIABLE) {
            Model model = solver.getModel();
            for (FuncDecl i : model.getConstDecls()) {
                testCase.put(i.getName().toString(), model.getConstInterp(i).toString());
            }
            return testCase;
        }
        else {
            return null;
        }
    }

    /*
     * private int countLeaves(MiNode root) { if (root.children.size() == 0) { return 1; } int count
     * = 0; for (MiNode child : root.children) { count += countLeaves(child); }
     * 
     * return count; }
     */

    /**
     * 由于不明原因, Z3的Context无法及时被垃圾回收, 造成内存泄漏, 所以手动刷新Context
     * 
     * @param root
     * @throws IOException
     */

    private void reFresh(MiNode root) throws IOException {
        // 每100刷新一次
        if (counter % 100 == 0) {
            Context old = root.ctx;
            old.close();
            substitute(root, new Context());
            System.out.println("refresh");

            this.trueTestCases.remove(null); // 约束中可能会出现空集的情况, 把空集去掉方便处理
            appendToFile("./true_constraint", trueTestCases);
            trueTestCases.clear();
        }
        counter++;
    }

    /**
     * 遍历语法树并替换Context
     * 
     * @param root
     * @param ctx
     */

    private void substitute(MiNode root, Context ctx) {
        root.ctx = ctx;
        if (root.children.size() == 0) {
            root.expr = ctx.mkBoolConst(root.value);
        }
        else if (root.value.equals("!")) {
            substitute(root.children.get(0), ctx);
            root.expr = ctx.mkNot(root.children.get(0).expr);
        }
        else if (root.value.equals("&")) {
            for (MiNode child : root.children) {
                substitute(child, ctx);
            }
            root.expr = ctx.mkAnd(root.children.get(0).expr, root.children.get(1).expr);
            for (int i = 2; i < root.children.size(); i++) {
                root.expr = ctx.mkAnd(root.expr, root.children.get(i).expr);
            }
        }
        else if (root.value.equals("|")) {
            for (MiNode child : root.children) {
                substitute(child, ctx);
            }
            root.expr = ctx.mkOr(root.children.get(0).expr, root.children.get(1).expr);
            for (int i = 2; i < root.children.size(); i++) {
                root.expr = ctx.mkOr(root.expr, root.children.get(i).expr);
            }
        }
        else {
            System.out.println("refresh error");
        }
    }
}
