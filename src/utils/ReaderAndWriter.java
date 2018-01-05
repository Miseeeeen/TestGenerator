package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;

import borMi.dataStructure.TestCase;

public class ReaderAndWriter {
    static int count = 1;

    public static void write(String path, HashSet<TestCase> testCases, String outputExpr) {
        try {
            FileWriter writer = new FileWriter(path, true);
            for (TestCase c : testCases) {
                writer.write("------------Test Case " + (count++) + "------------\r\n");
                writer.write("*** Input Variable ***" + "\r\n");
                writer.write(c.toString() + "\r\n");
                writer.write("*** Output Expr ***" + "\r\n");
                writer.write(outputExpr);
                writer.write("\r\n");
            }
            writer.close();
        } catch (Exception e) {
            System.out.println("ReaderAndWriter::write error!");
        }
    }
    
    /**
     * 修正联锁规则中的优先级问题, 强制加上括号
     * @param expr
     * @return
     */
    private static String filter(String expr) {
        return expr.replaceAll("\\)", "\\)\\)").replaceAll("\\(", "\\(\\(").replaceAll("\\|", "\\)\\|\\(");
    }


    public static ArrayList<String> load(String path) {
        // 读取path中的规则到list中
        File file = new File(path);
        BufferedReader reader = null;
        ArrayList<String> predicates = new ArrayList<String>();

        try {
            reader = new BufferedReader(new FileReader(file));

            String temp = null;
            temp = reader.readLine();

            while (temp != null) {
                String p = "";
                while (!temp.matches("[\n\r\t]*") && !temp.startsWith("&") && temp != null) {
                    p += temp;
                    temp = reader.readLine();
                }


                if (!p.matches("[\n\r\t]*") && !p.startsWith("&") && !p.contains("TIME")) {
                    // 这里应该加上filter的调用
                    String predicate = p.replaceAll("\n", "");
                    predicate = filter(predicate); // 强制dnf的括号优先级
                    predicates.add(predicate);
                }

                if (temp.matches("[\n\r\t]*") || temp.startsWith("&")) {
                    temp = reader.readLine();
                }
            }

            reader.close();
        } catch (IOException e) {
            System.out.println("lack the file: " + path);
        }

        return predicates;
    }

}
