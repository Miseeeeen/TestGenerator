package sat;

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
import borMi.visitor.PrintVisitor;
import utils.ReaderAndWriter;
import utils.TestCasesPrinter;

public class TestSAT {

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
            System.out.println(" 预处理中 " + i + "/" + input.size());
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

    public static HashSet<TestCase> getTestCases(BorNode root) {
        if (root.value.equals("true")) {
            HashSet<TestCase> set = new HashSet<TestCase>();
            set.add(new TestCase());
            return set;
        }

        if (root.value.equals("false")) {
            return new HashSet<TestCase>();
        }

        // 待优化
        List<BorNode> leaves = collectLeaves(root);

        String name = leaves.get(0).value;

        // 待优化
        BorNode copyTree1 = clone(root);
        BorNode copyTree2 = clone(root);

        sub(copyTree1, name, "true");
        update(copyTree1);

        HashSet<TestCase> set1 = getTestCases(copyTree1);
        for (TestCase c : set1) {
            c.put(name, "true");
        }

        sub(copyTree2, name, "false");
        update(copyTree2);

        HashSet<TestCase> set2 = getTestCases(copyTree2);
        for (TestCase c : set2) {
            c.put(name, "false");
        }

        HashSet<TestCase> set = new HashSet<TestCase>();
        set.addAll(set1);
        set.addAll(set2);

        return set;
    }

    public static void update(BorNode root) {
        if (root == null || root.getChildrenSize() == 0) {
            return;
        }

        if (root.value.equals("&")) {
            update(root.getFirstChild());
            update(root.getSecondChild());

            if (root.getFirstChild().value.equals("false")
                    || root.getSecondChild().value.equals("false")) {
                root.value = "false";
                root.children.clear();
                return;
            }
            else if (root.getFirstChild().value.equals("true")&&root.getSecondChild().value.equals("true")){
                root.value = "true";
                root.children.clear();
                return;
            }
            else if (root.getFirstChild().value.equals("true")) {
                root.value = root.getSecondChild().value;
                root.children = root.getSecondChild().children;
            }
            else if (root.getSecondChild().value.equals("true")) {
                root.value = root.getFirstChild().value;
                root.children = root.getFirstChild().children;
            }
        }
        else if (root.value.equals("!")) {
            update(root.getFirstChild());
            if (root.getFirstChild().value.equals("true")) {
                root.value = "false";
                root.children.clear();
                return;
            }
            else if (root.getFirstChild().value.equals("false")) {
                root.value = "true";
                root.children.clear();
                return;
            }
        }
        else if (root.value.equals("|")) {
            update(root.getFirstChild());
            update(root.getSecondChild());
            if (root.getFirstChild().value.equals("true")
                    || root.getSecondChild().value.equals("true")) {
                root.value = "true";
                root.children.clear();
                return;
            }
            else if (root.getFirstChild().value.equals("false")
                    && root.getSecondChild().value.equals("false")) {
                root.value = "false";
                root.children.clear();
                return;
            }
            else if (root.getFirstChild().value.equals("false")) {
                root.value = root.getSecondChild().value;
                root.children = root.getSecondChild().children;
            }
            else if (root.getSecondChild().value.equals("false")) {
                root.value = root.getFirstChild().value;
                root.children = root.getFirstChild().children;
            }
        }

    }

    public static BorNode clone(BorNode root) {
        if (root.getChildrenSize() == 0) {
            return new BorNode(root.value);
        }

        ArrayList<BorNode> children = new ArrayList<BorNode>();
        for (BorNode child : root.children) {
            children.add(clone(child));
        }

        BorNode clone = new BorNode();
        clone.value = root.value;
        clone.children = children;

        return clone;
    }

    public static void sub(BorNode root, String name, String value) {
        if (root.getChildrenSize() == 0) {
            if (root.value.equals(name)) {
                root.value = value;
            }
            return;
        }

        for (BorNode child : root.children) {
            sub(child, name, value);
        }
    }

    public static void main(String[] args) {

        ArrayList<String> test = ReaderAndWriter.load("./hxdc.txt");
        System.out.println(" 正在进行预处理");
        pre_proc(test); // 预处理, 将某条变量计算之前出现的所有变量替换为上一个周期的变量

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

        
        HashMap<String, Integer> callMap = new HashMap<String, Integer>();
        // 代换开始
        System.out.println("    开始构建约束");
        for (int i = 0; i < test.size(); i++) {
            System.out.println("    约束构建中 " + i + "/" + (test.size() - 1));

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
                        leaf.value = interTree.value;
                        leaf.children = interTree.children;
                    }
                }
            }
        }
        
        //max(callMap);

        for(int i=0; i<test.size(); i++) {
            String variable = getLeft(test.get(i));
            System.out.println("消元中 -> "+variable + " "+i + "/" + (test.size()-1));
            BorNode root = treeMap.get(variable);
            HashSet<TestCase> testCases1 = getTestCases(root);
            testCases1.clear();
        }
        //System.out.println("代换的变量数 -> "+count+"/"+test.size());
    }

    // 获得嵌套层数
    public static int getLevel(HashMap<String, Integer> callMap, String variableName, ArrayList<String> test) {
        int variableID = 0;
        for(int i=0; i<test.size(); i++) {
            if(getLeft(test.get(i)).equals(variableName)) {
                variableID = i;
            }
        }
        int max = 0;
        for(int j=0; j<variableID; j++) {
            max = Math.max(max, callMap.containsKey(variableID+","+j)?callMap.get(variableID+","+j):0);
        }
        return max;
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
