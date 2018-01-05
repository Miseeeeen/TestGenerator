package genetic;

import borMi.dataStructure.TestCase;
import borMi.parser.BorNode;
import borMi.parser.BorParser;
import borMi.parser.Scanner;

public class FitnessCalculator {

    public static double calc(Individual individual) {
        TestCase c = Decoder.decode(individual);
        double mutationScore = mutationScore(c, individual.getBoolExpr());
        individual.setFitness(mutationScore);
        return mutationScore;
    }

    public static double mutationScore(TestCase c, String boolExpr) {
        double score = 0;
        int times = 50;

        for (int i = 0; i < times; i++) {
            String normalExpr = boolExpr;
            String mutatedExpr = mutate(c, normalExpr);
            if (isKilled(c, normalExpr, mutatedExpr)) {
                score++;
            }
        }

        return score / times;
    }

    // 待优化
    // 也可以针对不同的变异算子来计算
    private static String mutate(TestCase c, String normalExpr) {

        int random1 = (int) ((Math.random() * 100) % c.size());
        int random2 = (int) ((Math.random() * 100) % c.size());

        int i = 0;
        String src = null;
        String dst = null;
        for (String name : c.variableSet()) {
            if (i == random1) {
                src = name;
            }
            if (i == random2) {
                dst = name;
            }

            i++;
        }

        return replace(normalExpr, src, dst);
    }

    /**
     * 替换normalExpr中的src变量为dst
     * 构建语法树, 替换叶结点
     * 
     * @param normalExpr
     * @param src
     * @param dst
     * @return
     */

    // 性能提升点
    private static String replace(String normalExpr, String src, String dst) {
        BorParser ps1 = new BorParser(new Scanner(normalExpr + ";"));
        BorNode root = ps1.getAST();
        replace(root, src, dst);

        return root.toString();
    }

    private static void replace(BorNode root, String src, String dst) {
        if (root == null) {
            return;
        }

        if (root.children.size() == 0 && root.value.equals(src)) {
            root.value = dst;
        }
        else if (root.children.size() == 1) {
            replace(root.getFirstChild(), src, dst);
        }
        else if (root.children.size() == 2) {
            replace(root.getFirstChild(), src, dst);
            replace(root.getSecondChild(), src, dst);
        }
    }

    private static boolean isKilled(TestCase c, String normalExpr, String mutatedExpr) {
        BorParser ps1 = new BorParser(new Scanner(normalExpr + ";"));
        BorNode normalTree = ps1.getAST();
        BorParser ps2 = new BorParser(new Scanner(mutatedExpr + ";"));
        BorNode mutatedTree = ps2.getAST();

        if (eval(c, normalTree) != eval(c, mutatedTree)) {
            return true;
        }
        else {
            return false;
        }
    }

    private static boolean eval(TestCase c, BorNode root) {
        if (root.children.size() == 0) {
            if (c.get(root.value).equals("true")) {
                return true;
            }
            else if (c.get(root.value).equals("false")) {
                return false;
            }
            else {
                System.out.println("MutationTester::eval Wrong!");
                return false;
            }
        }
        else if (root.value.equals("!")) {
            return !eval(c, root.getFirstChild());
        }
        else if (root.value.equals("&")) {
            return eval(c, root.getFirstChild()) && eval(c, root.getSecondChild());
        }
        else if (root.value.equals("|")) {
            return eval(c, root.getFirstChild()) || eval(c, root.getSecondChild());
        }
        else {
            System.out.println("MutationTester::eval Wrong!");
            return true;
        }
    }

}
