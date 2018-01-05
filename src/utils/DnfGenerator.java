package utils;

import java.util.ArrayList;
import java.util.Random;

/**
 * Expression -> Term '|' Expression -> Term '|' Term
 * 
 * Term -> '!' 'Name' '&' 'Name'
 * 
 * 乱, 待修改
 * 
 * @author misen
 *
 */
public class DnfGenerator {
    private static int size = 0;

    /**
     * 给定变量个数, 随机生成相应的DNF
     * @param variableSize
     * @return
     */
    public static String getDnf(int variableSize) {
        size = variableSize;
        return Expression();
    }

    private static String Expression() {
        if (getRandomInt(3) != 0) {
            return "(" + Term() + ") |" + Expression();
        }
        else if (getRandomInt(3) != 0) {
            return "(" + Term() + ")|" + Expression();
        }
        else if (getRandomInt(3) != 0) {
            return "(" + Term() + ")|" + Expression();
        }
        else {
            return "(" + Term() + ")|(" + Term() + ")";
        }
    }

    private static String Term() {
        String term = "";
        ArrayList<String> b = new ArrayList<String>();
        for (int i = 0; i < size; i++) { // 控制size
            b.add("B" + i + "b");
        }

        int loop = getRandomInt(size); // 控制size

        for (int i = 0; i < loop - 1; i++) {
            if (getRandomInt(2) == 0) { // 非
                term += "!" + b.remove(getRandomInt(b.size())) + "&";

            }
            else {
                term += b.remove(getRandomInt(b.size())) + "&";
            }
        }

        if (term.length() == 0) {
            term = "B1b";
        }
        else {
            term = term.substring(0, term.length() - 1);
        }

        return term;
    }

    private static int getRandomInt(int bound) {
        return new Random().nextInt(bound);
    }

}
