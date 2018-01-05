package genetic;

import borMi.dataStructure.TestCase;
import borMi.parser.BorNode;
import borMi.parser.BorParser;
import borMi.parser.Scanner;

public class Decoder {

    public static TestCase decode(Individual individual) {
        return decode(individual.getChromosome(), individual.getBoolExpr());
    }

    public static TestCase decode(int[] chromosome, String boolExpr) {
        BorNode root = getAst(boolExpr);
        TestCase c = new TestCase();
        initTestCase(root, c);

        int i = 0;
        for (String name : c.variableSet()) {
            c.put(name, chromosome[i++] == 0 ? "false" : "true");
        }

        return c;
    }

    private static BorNode getAst(String boolExpr) {
        Scanner sc = new Scanner(boolExpr + ";");
        BorParser parser = new BorParser(sc);
        return parser.getAST();
    }

    private static void initTestCase(BorNode root, TestCase c) {
        if (root == null) {
            return;
        }

        if (root.children.size() == 0) {
            c.put(root.value, "true");
        }
        else if(root.children.size()==1){
            initTestCase(root.getFirstChild(), c);
        }
        else{
            initTestCase(root.getFirstChild(), c);
            initTestCase(root.getSecondChild(), c);
        }
    }

}
