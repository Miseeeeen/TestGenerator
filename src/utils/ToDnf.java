package utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;


/**
 * 调用Python库完成转换成DNF的任务
 * 
 * @author misen
 *
 */
public class ToDnf {

    public static String toDnf(String expr) {
        expr = encode(expr);
        String result = callPython(expr);
        return transform(decode(result));
    }
    
    /**
     * python的库函数支持 "~"形式的not, 不支持"!"的not, 同时"-"会引起歧义, 误认为是算数中的减号, 所以对输入进行编码映射, 解码是个逆过程
     * @param expr
     * @return
     */

    private static String encode(String expr) {
        return expr.replaceAll("!", "~").replaceAll("-", "_");
    }

    private static String decode(String expr) {
        return expr.replaceAll("_", "-").replaceAll(" ", "");
    }
    
    /**
     * 把expr写到tmp里传递表达式
     * @param expr
     * @return
     */

    private static String callPython(String expr) {
        String result = "";
        try {
            FileWriter writer = new FileWriter("./tmp", true);
            writer.append(expr);
            writer.close();

            Process process = Runtime.getRuntime().exec("python ./to_dnf.py");
            InputStreamReader ir = new InputStreamReader(process.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            result = input.readLine();
            input.close();
            ir.close();

            File file = new File("./tmp");
            file.delete();
        } catch (IOException e) {

        }

        return result;
    }

    /**
     * S表达式转化成中缀形式的DNF
     * @param expr
     * @return
     */
    private static String transform(String expr) {
        if (expr.startsWith("Or")) {
            String dnf = "";

            expr = expr.substring(3, expr.length() - 1);
            int par = 0;
            int start = 0;
            for (int i = 0; i < expr.length(); i++) {
                if (par == 0 && expr.charAt(i) == ',') {
                    String term = expr.substring(start, i);
                    dnf += "|" + "(" + term(term) + ")";
                    start = i + 1;
                }
                else if (i == expr.length() - 1) {
                    String term = expr.substring(start);
                    dnf += "|" + "(" + term(term) + ")";
                }
                else if (expr.charAt(i) == '(') {
                    par++;
                }
                else if (expr.charAt(i) == ')') {
                    par--;
                }
            }

            return dnf.substring(1);
        }
        else if (expr.startsWith("And")) {
            return term(expr);
        }
        else {
            throw new Error("error!");
        }
    }

    private static String term(String term) {
        if (term.startsWith("And")) {
            term = term.substring(4, term.length() - 1);
            String[] literals = term.split(",");

            term = "";
            for (String l : literals) {
                if (l.contains("Not")) {
                    term += "&" + l.substring(4, l.length() - 1);
                }
                else {
                    term += "&" + l;
                }
            }

            return term.substring(1);
        }
        else {
            return term;
        }
    }

}
