package activityGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.FuncDecl;
import com.microsoft.z3.Model;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;

import borMi.dataStructure.TestCase;
import borMi.parser.BorNode;
import borMi.parser.BorParser;
import borMi.parser.Scanner;
import utils.ReaderAndWriter;

public class Replacer {

    private static String getRight(String pr) {
        pr = pr.replaceAll(" ", "");
        return pr.substring(pr.indexOf('=') + 1);
    }

    private static String getLeft(String pr) {
        pr = pr.replaceAll(" ", "");
        return pr.substring(0, pr.indexOf('='));
    }

    private static String merge(String src, String txt, String dst) { // 要把"B&C"中的B替换成b1&b2, src是B,
                                                                      // txt是b1&b2, dst是"B&C"
        // 这段代码回头改
        dst = dst.replaceAll(" ", "");
        // 这里替换时, 优先级的问题要考虑一下

        // 切割
        String split = "|&!()";
        ArrayList<String> tmpp = new ArrayList<String>(); // 存储按照split切块的字符串

        for (int i = 0; i < dst.length(); i++) {
            if (split.contains(dst.charAt(i) + "")) {
                tmpp.add(dst.charAt(i) + "");
            }
            else {
                StringBuffer buffer = new StringBuffer();
                while (!split.contains(dst.charAt(i) + "")) {
                    buffer.append(dst.charAt(i));
                    i++;
                    if (i >= dst.length()) {
                        break;
                    }
                }
                tmpp.add(buffer.toString());
                i--;
            }
        }

        for (int i = 0; i < tmpp.size(); i++) {
            if (tmpp.get(i).equals(src)) {
                tmpp.set(i, txt);
            }
        }

        String result = "";

        for (String i : tmpp) {
            result += i;
        }

        return result;
    }

    private static void pre_proc(ArrayList<String> input) {
        // 若有"B=A&B; A=B;" 则B的计算规则中的A, B为上个周期的值

        for (int i = 0; i < input.size(); i++) {
            String left = getLeft(input.get(i));
            //System.out.println("    预处理中 " + i + "/" + input.size());
            for (int j = 0; j < i + 1; j++) {
                input.set(j, getLeft(input.get(j)) + "="
                        + merge(left, left + "_last_cycle", getRight(input.get(j))));
            }
        }
    }

    public static BorNode getTree(String expr) {
        Scanner sc = new Scanner(expr + ";");
        BorParser ps = new BorParser(sc);
        BorNode root = ps.getAST();
        return root;
    }

    public static HashMap<String, BoolExpr> replace(String path) {
        ArrayList<String> test = ReaderAndWriter.load(path);

        System.out.println("    正在进行预处理");
        pre_proc(test); // 预处理, 将某条变量计算之前出现的所有变量替换为上一个周期的变量
        System.out.println("    预处理完毕");

        HashMap<String, BorNode> treeMap = new HashMap<String, BorNode>();

        System.out.println("    正在进行代换初始化");
        // 初始化
        for (String pr : test) {
            String variable = getLeft(pr);
            String expr = getRight(pr);
            System.out.println(expr);
            BorNode root = getTree(expr);
            root.buildBoolExpr();
            treeMap.put(variable, root);
        }
        System.out.println("    代换初始化完毕");

        // 代换开始

        HashMap<String, Integer> callMap = new HashMap<String, Integer>(); // 统计嵌套层数用

        System.out.println("    开始构建约束");
        for (int i = 0; i < test.size(); i++) {
            System.out.println("    约束构建中 "+ i + "/" + (test.size() - 1));
            System.out.println("        目标表达式为 "+test.get(i));

            String variable = getLeft(test.get(i));
            List<BorNode> leaves = collectLeaves(treeMap.get(variable)); // 叶结点才可能出现中间变量

            for (int j = 0; j < i; j++) {
                String interM = getLeft(test.get(j));
                for (BorNode leaf : leaves) {
                    if (leaf.value.equals(interM)) {
                        System.out.println("        发现中间变量 -> "+interM);
                        callMap.put(i + "," + j, 1);

                        BorNode interTree = treeMap.get(interM);
                        leaf.boolExpr = interTree.boolExpr;
                        // 直接复制约束, 因为是叶结点, 重建时应该也没问题
                    }
                }
            }

            BorNode tree = treeMap.get(variable);
            tree.buildBoolExpr();
            //constraintSolving(BorNode.ctx, tree.boolExpr);
        }
        System.out.println("    约束构建完毕");
        
        HashMap<String, BoolExpr> boolMap = new HashMap<String, BoolExpr>();
        for(String name: treeMap.keySet()) {
            boolMap.put(name, treeMap.get(name).boolExpr);
        }
        return boolMap;

        //max(callMap);
    }

    public static void max(HashMap<String, Integer> callMap) {
        int max = 0;

        for (int i = 0; i < 15000; i++) {
            for (int j = 0; j < i; j++) {
                if (callMap.containsKey(i + "," + j)) {
                    callMap.put(i + "," + j, max(callMap, j) + 1);
                    max = Math.max(max, callMap.get(i + "," + j));
                }
            }
        }

        System.out.println("max is " + max);
    }

    public static int max(HashMap<String, Integer> callMap, int j) {
        int max = 0;
        for (int k = 0; k < 15000; k++) {
            if (callMap.containsKey(j + "," + k)) {
                max = Math.max(max, callMap.get(j + "," + k));
            }
        }
        return max;
    }

    public static void constraintSolving(Context ctx, BoolExpr expr) {
        Solver solver = ctx.mkSolver();
        solver.add(expr);

        if (solver.check() == Status.SATISFIABLE) {
            Model model = solver.getModel();
            for (FuncDecl i : model.getConstDecls()) {
                System.out.println(i.getName() + " " + model.getConstInterp(i).toString());
            }
            System.out.println("Constraint solving OK!");
        }
        else {
            System.out.println("约束无解");
        }
    }

    // 改成非递归可以提升性能
    public static List<BorNode> collectLeaves(BorNode root) {
        List<BorNode> leaves = new ArrayList<BorNode>();

        if (root.children.size() == 0) {
            leaves.add(root);
            return leaves;
        }
        else if (root.children.size() == 1) {
            return collectLeaves(root.getFirstChild());
        }
        else {
            leaves.addAll(collectLeaves(root.getFirstChild()));
            leaves.addAll(collectLeaves(root.getSecondChild()));
            return leaves;
        }
    }
}
