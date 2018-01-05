package genetic;

import java.util.HashSet;

import borMi.parser.BorNode;
import borMi.parser.BorParser;
import borMi.parser.Scanner;

public class Encoder {

    public static int[] initChromosome(String boolExpr) {
        BorNode root = getAst(boolExpr);
        HashSet<String> set = new HashSet<String>();
        collectLeaves(root, set);

        int[] chromosome = new int[set.size()];
        randomShift(chromosome);
        return chromosome;
    }

    private static void randomShift(int[] chromosome) {
        for (int i = 0; i < chromosome.length; i++) {
            chromosome[i] = Math.random() > 0.5 ? 1 : 0;
        }
    }

    private static BorNode getAst(String boolExpr) {
        Scanner sc = new Scanner(boolExpr + ";");
        BorParser parser = new BorParser(sc);
        return parser.getAST();
    }

    private static void collectLeaves(BorNode root, HashSet<String> set) {
        if (root == null) {
            return;
        }

        if (root.children.size() == 0) {
            set.add(root.value);
        }
        else if(root.children.size()==1){
            collectLeaves(root.getFirstChild(), set);
        }
        else{
            collectLeaves(root.getFirstChild(), set);
            collectLeaves(root.getSecondChild(), set);
        }
    }

}
