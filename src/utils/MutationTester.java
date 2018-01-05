package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Stack;

import borMi.dataStructure.TestCase;
import borMi.parser.BorNode;
import borMi.parser.BorParser;
import borMi.parser.MiNode;
import borMi.parser.Scanner;

/**
 * 定义了若干变异算子
 * 
 * @author misen
 *
 */

public class MutationTester {
    public static int getRandomInt(int bound) {
        return new Random().nextInt(bound);
    }


    public static String orfStar(String pr) {
        ArrayList<Integer> and_index = new ArrayList<Integer>();
        for (int i = 0; i < pr.length(); i++) {
            if (pr.charAt(i) == '&') {
                and_index.add(i);
            }
        }

        int random = getRandomInt(and_index.size());
        int index = and_index.get(random);
        pr = pr.substring(0, index) + "|" + pr.substring(index + 1);

        return pr;
    }

    public static String tnf(String pr) {
        String[] tmp = pr.split("\\|");

        int random = getRandomInt(tmp.length - 1);
        pr = "";

        for (int i = 0; i < tmp.length; i++) {
            if (i == random) {
                tmp[i] = "!" + tmp[i];
            }
            pr = pr + "|" + tmp[i];
        }

        return pr.substring(1);
    }

    public static String orfPlus(String pr) {
        ArrayList<Integer> or_index = new ArrayList<Integer>();
        for (int i = 0; i < pr.length(); i++) {
            if (pr.charAt(i) == '|') {
                or_index.add(i);
            }
        }

        int random = getRandomInt(or_index.size());
        int index = or_index.get(random);
        pr = pr.substring(0, index) + "&" + pr.substring(index + 1);

        return pr;
    }

    public static String tof(String pr) {
        String[] tmp = pr.split("\\|");

        int random = getRandomInt(tmp.length - 1);
        pr = "";

        for (int i = 0; i < tmp.length; i++) {
            if (i == random) continue;
            pr = pr + "|" + tmp[i];
        }

        return pr.substring(1);
    }

    public static String lof(String pr) {
        String[] tmp = pr.split("\\|");

        int random = getRandomInt(tmp.length - 1);
        String term = tmp[random];

        while (!term.contains("&")) {
            random = getRandomInt(tmp.length - 1);
            term = tmp[random];
        }

        String[] tmp2 = term.substring(1, term.length() - 1).split("&");

        term = "";
        int j = getRandomInt(tmp2.length);
        for (int i = 0; i < tmp2.length; i++) {
            if (tmp2[i].contains(")")) {
                tmp2[i] = tmp2[i].substring(0, tmp2[i].length() - 1);
            }
            if (tmp2[i].contains("(")) {
                tmp2[i] = tmp2[i].substring(1);
            }
            if (j == i) {
                continue;
            }

            term = term + "&" + tmp2[i];
        }

        term = "(" + term.substring(1) + ")";
        pr = "";
        for (int i = 0; i < tmp.length; i++) {
            if (i != random) {
                pr = pr + "|" + tmp[i];
            }
        }
        pr = pr + "|" + term;
        return pr.substring(1);
    }

    public static String enf(String pr) {
        String[] tmp = pr.split("\\|");

        int random = getRandomInt(tmp.length - 1);
        pr = "";

        for (int i = 0; i < tmp.length; i++) {
            if (i == random) {
                tmp[i] = "(" + "!" + tmp[i] + ")";
            }
            pr = pr + "|" + tmp[i];
        }

        return pr.substring(1);
    }

    public static Stack<String> collectLiteral(BorNode root, Stack<String> stack) {
        if (root.children.size() == 0) {
            stack.push(root.value);
            return stack;
        }

        for (BorNode child : root.children) {
            collectLiteral(child, stack);
        }

        return stack;
    }

    public static String lrf(String pr) {
        String[] tmp = pr.split("\\|");

        int random = getRandomInt(tmp.length - 1);
        String term = tmp[random];
        String[] tmp2 = term.substring(1, term.length() - 1).split("&");

        BorParser parser = new BorParser(new Scanner(pr + ";"));
        Stack<String> literals = collectLiteral(parser.getAST(), new Stack<String>());
        int random2 = getRandomInt(literals.size() - 1);
        int random3 = getRandomInt(tmp2.length);


        term = "";
        for (int i = 0; i < tmp2.length; i++) {
            if (tmp2[i].contains(")")) {
                tmp2[i] = tmp2[i].substring(0, tmp2[i].length() - 1);
            }
            if (i == random3) {
                tmp2[i] = literals.elementAt(random2);
            }

            term = term + "&" + tmp2[i];
        }

        term = "(" + term.substring(1) + ")";
        pr = "";
        for (int i = 0; i < tmp.length; i++) {
            if (i != random) {
                pr = pr + "|" + tmp[i];
            }
        }
        pr = pr + "|" + term;
        return pr.substring(1);
    }

    public static String lif(String pr) {
        String[] tmp = pr.split("\\|");

        int random = getRandomInt(tmp.length - 1);
        String term = tmp[random];
        String[] tmp2 = term.substring(1, term.length() - 1).split("&");

        BorParser parser = new BorParser(new Scanner(pr + ";"));
        Stack<String> literals = collectLiteral(parser.getAST(), new Stack<String>());
        int random2 = getRandomInt(literals.size() - 1);

        term = "";
        for (int i = 0; i < tmp2.length; i++) {
            if (tmp2[i].contains(")")) {
                tmp2[i] = tmp2[i].substring(0, tmp2[i].length() - 1);
            }
            if (tmp2[i].contains("(")) {
                tmp2[i] = tmp2[i].substring(1);
            }
            term = term + "&" + tmp2[i];
        }

        // 防止等价变异
        while (term.contains(literals.elementAt(random2))) {
            random2 = getRandomInt(literals.size() - 1);
        }

        term = "(" + term.substring(1) + "&" + literals.elementAt(random2) + ")";
        pr = "";
        for (int i = 0; i < tmp.length; i++) {
            if (i != random) {
                pr = pr + "|" + tmp[i];
            }
        }
        pr = pr + "|" + term;
        return pr.substring(1);
    }


    public static String lnf(String pr) {
        String[] tmp = pr.split("\\|");

        int random = getRandomInt(tmp.length - 1);
        String term = tmp[random];
        String[] tmp2 = term.substring(1, term.length() - 1).split("&");

        term = "";
        int j = 0;
        for (int i = 0; i < tmp2.length; i++) {
            if (tmp2[i].contains(")")) {
                tmp2[i] = tmp2[i].substring(0, tmp2[i].length() - 1);
            }
            if (tmp2[i].contains("(")) {
                tmp2[i] = tmp2[i].substring(1);
            }
            if (j == 0 && !tmp2[i].contains("!")) {
                tmp2[i] = "!" + tmp2[i];
                j++;
            }

            term = term + "&" + tmp2[i];
        }

        term = "(" + term.substring(1) + ")";
        pr = "";
        for (int i = 0; i < tmp.length; i++) {
            if (i != random) {
                pr = pr + "|" + tmp[i];
            }
        }
        pr = pr + "|" + term;
        return pr.substring(1);
    }

    // MutationTester的main
    public static double getScore(String pr, String truePath, String falsePath) {
        int times = 200;
        int count = 0;

        for (int i = 0; i < times; i++) {
            if (isKilled(pr, truePath, falsePath)) {
                count++;
            }
            System.out.println("times: " + (i + 1) + " killed: " + count);
        }

        return count / times;
    }

    public static HashSet<TestCase> convertToTestCases(ArrayList<String> testCases) {
        HashSet<TestCase> result = new HashSet<TestCase>();

        for (String pr : testCases) {
            TestCase c = new TestCase();
            String[] pair = pr.split(" ");
            for (String i : pair) {
                String[] tmp = i.split(":");
                c.put(tmp[0], tmp[1]);
            }

            result.add(c);
        }

        return result;
    }

    public static boolean eval(TestCase c, BorNode root) {
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

    public static boolean isKilled(String pr, String truePath, String falsePath) {
        HashSet<TestCase> trueTestCases = convertToTestCases(ReaderAndWriter.load(truePath));
        HashSet<TestCase> falseTestCases = convertToTestCases(ReaderAndWriter.load(falsePath));

        pr = lrf(pr);

        BorParser ps = new BorParser(new Scanner(pr + ";"));
        BorNode root = ps.getAST();

        for (TestCase c : trueTestCases) {
            if (eval(c, root) != true) {
                return true;
            }
        }

        for (TestCase c : falseTestCases) {
            if (eval(c, root) != false) {
                return true;
            }
        }

        return false;
    }

}
