package activityGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.FuncDecl;
import com.microsoft.z3.Model;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;

import borMi.dataStructure.TestCase;
import borMi.parser.BorNode;
import genetic.BooleanGA;
import processor.MiProcessorM;
import utils.TestCasesPrinter;
import utils.ReaderAndWriter;
import utils.ToDnf;

public class ActivityMain {

    public static void main(String[] args) {
        
        Scanner sc = new Scanner(System.in);
        System.out.println("请指定活动图路径: ");
        String graphPath = sc.nextLine();
        System.out.println("请指定规则路径: ");
        String boolPath = sc.nextLine();
        System.out.println("请指定测试用例输出路径: ");
        String testCasePath = sc.nextLine();

        graphPath = "./ZSG.uml";
        boolPath = "./ZSG.txt";
        testCasePath = "./testCases.tc";

        GraphParser parser = new GraphParser();
        System.out.println("正在解析活动图");
        Graph graph = parser.parse(graphPath);
        System.out.println("解析成功");

        System.out.println("正在解析路径表达式");
        List<PathConstraint> paths = pathConstraints(graph.getInitialNode());
        System.out.println("解析成功");

        System.out.println("正在代换中间变量"); 
        HashMap<String, BoolExpr> boolMap = Replacer.replace(boolPath);
        System.out.println("代换成功");
        
        System.out.println("正在为路径表达式生成测试用例");
        //constraintSolving(BorNode.ctx, boolMap.get("T13D-SLJ"));
        
        System.out.println(paths.size());
        for (int i=0; i<paths.size(); i++) {
            System.out.println("    路径处理中 "+ (i+1) + "/" + paths.size());
            PathConstraint p = paths.get(i);

            String inputExpr = filter(p.inputExpr.substring(0, p.inputExpr.length() - 1)); // inputExpr最后是一个与
            String outputExpr = p.outputExpr.substring(1); // outputExpr开头是一个与

            System.out.println("    输入表达式 -> "+inputExpr);
            System.out.println("    输出表达式 -> "+outputExpr);

            System.out.println("    正在转换成析取范式");
            String dnf = ToDnf.toDnf(inputExpr);
            System.out.println("    转换完毕");
            System.out.println("    Meaningful Impact处理中");
            MiProcessorM mi = new MiProcessorM(dnf);
            System.out.println("    Meaningful Impact处理完毕");

            HashSet<TestCase> testCases = new HashSet<TestCase>();
            for (TestCase c : mi.trueTestCases) {
                Context ctx = BorNode.ctx;
                BoolExpr boolExpr = ctx.mkTrue();

                for (String name : c.variableSet()) {
                    if (isIntermediateVar(name, boolMap)) {
                        if (c.isTrue(name)) {
                            boolExpr = ctx.mkAnd(boolExpr, boolMap.get(name));
                        }
                        else {
                            boolExpr = ctx.mkAnd(boolExpr, ctx.mkNot(boolMap.get(name)));
                        }
                    }
                    else {
                        if (c.isTrue(name)) {
                            boolExpr = ctx.mkAnd(boolExpr, ctx.mkBoolConst(name));
                        }
                        else {
                            boolExpr = ctx.mkAnd(boolExpr, ctx.mkNot(ctx.mkBoolConst(name)));
                        }
                    }
                }
                
                testCases.add(constraintSolving(ctx, boolExpr)); // 这句调用可能会引发Bug
            }

            if(testCases.size()==0) { //  补上一个true point
                testCases.add(anotherAlgo(inputExpr, boolMap));
            }

            testCases.remove(null);

            //ConstraintsPrinter.print(constraints);
            // constraints 和outputExpr组成测试用例
            
            //System.out.println("testCases -> " + constraints.size());

            System.out.println("    正在写入测试用例");
            ReaderAndWriter.write(testCasePath, testCases, outputExpr);
            System.out.println("    写入完毕");
        }
        System.out.println("生成完毕");
    }
    
    private static TestCase anotherAlgo(String inputExpr, HashMap<String, BoolExpr> boolMap) {
        BorNode root = Replacer.getTree(inputExpr);
        traverse(root, boolMap); // traverse时替换叶结点的boolExpr
        
        root.buildBoolExpr();
        
        return constraintSolving(BorNode.ctx, root.boolExpr);
    }

    private static void traverse(BorNode root, HashMap<String, BoolExpr> boolMap) {
        if(root.children.size()==0) {
            if(boolMap.containsKey(root.value)) {
                root.boolExpr = boolMap.get(root.value);
            }
        }
        else if(root.children.size()==1) {
            traverse(root.getFirstChild(), boolMap);
        }
        else if(root.children.size()==2) {
            traverse(root.getFirstChild(), boolMap);
            traverse(root.getSecondChild(), boolMap);
        }

        return;
    }

    private static boolean isIntermediateVar(String name, HashMap<String, BoolExpr> boolMap) {
        return boolMap.keySet().contains(name);
    }

    public static String filter(String expr) {
        return expr.replaceAll("&amp;", "&");
    }

    public static List<PathConstraint> pathConstraints(Node node) {
        if (node.isFinalNode()) {
            List<PathConstraint> paths = new ArrayList<PathConstraint>();
            paths.add(new PathConstraint("", ""));
            return paths;
        }

        List<PathConstraint> paths = new ArrayList<PathConstraint>();
        for (Edge outgoing : node.outgoings) {
            List<PathConstraint> tmp = pathConstraints(outgoing.target);

            for (PathConstraint p : tmp) {
                PathConstraint pp = p.clone();
                if (outgoing.target.isOutputNode()) {
                    pp.outputExpr += "&" + outgoing.target.name;
                }

                if (outgoing.guardExpr.equals("true")) {
                    paths.add(pp);
                }
                else {
                    pp.inputExpr = "(" + outgoing.guardExpr + ")" + "&" + pp.inputExpr;
                    paths.add(pp);
                }
            }
        }

        return paths;
    }

    // 这个函数肯定有坑, Z3可能不给出一部分变量的值, 待修正
    private static TestCase constraintSolving(Context ctx, BoolExpr expr) {
        Solver solver = ctx.mkSolver();
        solver.add(expr);

        TestCase testCase = new TestCase();
        /*
         * for (String i : defaultConstraint.keySet()) { constraint.put(i, "true"); }
         */

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
